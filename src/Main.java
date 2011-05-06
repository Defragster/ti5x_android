package nz.gen.geek_central.ti5x;
/*
    ti5x calculator emulator -- mainline

    Copyright 2011 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    This program is free software: you can redistribute it and/or modify it under
    the terms of the GNU General Public License as published by the Free Software
    Foundation, either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
    A PARTICULAR PURPOSE. See the GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

public class Main extends android.app.Activity
  {
    java.util.Map<android.view.MenuItem, Runnable> OptionsMenu;

    interface RequestResponseAction /* response to an activity result */
      {
        public void Run
          (
            int ResultCode,
            android.content.Intent Data
          );
      } /*RequestResponseAction*/

    java.util.Map<Integer, RequestResponseAction> ActivityResultActions;

  /* request codes, all arbitrarily assigned */
    final int LoadProgramRequest = 1;
    final int ImportDataRequest = 2;
    final int SaveProgramRequest = 3;
    final int ExportDataRequest = 4;

    final int SwitchSaveAs = android.app.Activity.RESULT_FIRST_USER + 0;
    final int SwitchAppend = android.app.Activity.RESULT_FIRST_USER + 1;
    boolean ExportAppend;

    android.view.ViewGroup PickerExtra, SaveAsExtra;
    boolean ShuttingDown = false;
    boolean StateLoaded = false; /* will be reset to false every time activity is killed and restarted */

    class ReplaceConfirm
        extends android.app.AlertDialog
        implements android.content.DialogInterface.OnClickListener
      {

        public ReplaceConfirm
          (
            android.content.Context ctx,
            int MsgID
          )
          {
            super(ctx);
            setMessage(ctx.getString(MsgID));
            setButton
              (
                android.content.DialogInterface.BUTTON_POSITIVE,
                ctx.getString(R.string.replace),
                this
              );
            setButton
              (
                android.content.DialogInterface.BUTTON_NEGATIVE,
                ctx.getString(R.string.cancel),
                this
              );
          } /*ReplaceConfirm*/

        @Override
        public void onClick
          (
            android.content.DialogInterface TheDialog,
            int WhichButton
          )
          {
            if (WhichButton == android.content.DialogInterface.BUTTON_POSITIVE)
              {
                LaunchImportPicker();
              } /*if*/
            dismiss();
          } /*onClick*/

      } /*ReplaceConfirm*/

    void LaunchImportPicker()
      {
        final Picker.PickerAltList[] OnlyAlt =
            {
                new Picker.PickerAltList
                  (
                    /*RadioButtonID =*/ 0,
                    /*Prompt =*/ getString(R.string.import_prompt),
                    /*NoneFound =*/ getString(R.string.no_data_files),
                    /*FileExt =*/ "",
                    /*SpecialItem =*/ null
                  ),
            };
        Picker.Launch
          (
            /*Acting =*/ Main.this,
            /*RequestCode =*/ ImportDataRequest,
            /*Extra =*/ null,
            /*LookIn =*/ Persistent.ExternalDataDirectories,
            /*AltLists =*/ OnlyAlt
          );
      } /*LaunchImportPicker*/

    void LaunchExportPicker()
      {
        SaveAsExtra = (android.view.ViewGroup)
            getLayoutInflater().inflate(R.layout.save_append, null);
        SaveAsExtra.findViewById(R.id.switch_append).setOnClickListener
          (
            new android.view.View.OnClickListener()
              {
                public void onClick
                  (
                    android.view.View TheView
                  )
                  {
                    SaveAs.Current.setResult(SwitchAppend);
                    SaveAs.Current.finish();
                  } /*onClick*/
              } /*OnClickListener*/
          );
        PickerExtra = (android.view.ViewGroup)
            getLayoutInflater().inflate(R.layout.save_new, null);
        PickerExtra.findViewById(R.id.switch_new).setOnClickListener
          (
            new android.view.View.OnClickListener()
              {
                public void onClick
                  (
                    android.view.View TheView
                  )
                  {
                    Picker.Current.setResult(SwitchSaveAs);
                    Picker.Current.finish();
                  } /*onClick*/
              } /*OnClickListener*/
          );
        if (ExportAppend)
          {
            final Picker.PickerAltList[] OnlyAlt =
                {
                    new Picker.PickerAltList
                      (
                        /*RadioButtonID =*/ 0,
                        /*Prompt =*/ getString(R.string.export_prompt),
                        /*NoneFound =*/ getString(R.string.no_data_files),
                        /*FileExt =*/ "",
                        /*SpecialItem =*/ null
                      ),
                };
            Picker.Launch
              (
                /*Acting =*/ Main.this,
                /*RequestCode =*/ ExportDataRequest,
                /*Extra =*/ PickerExtra,
                /*LookIn =*/ Persistent.ExternalDataDirectories,
                /*AltLists =*/ OnlyAlt
              );
          }
        else
          {
            SaveAs.Launch
              (
                /*Acting =*/ Main.this,
                /*RequestCode =*/ ExportDataRequest,
                /*SaveWhat =*/ getString(R.string.exported_data),
                /*Extra =*/ SaveAsExtra,
                /*FileExt =*/ ""
              );
          } /*if*/
      } /*LaunchExportPicker*/

    @Override
    public boolean onCreateOptionsMenu
      (
        android.view.Menu TheMenu
      )
      {
        OptionsMenu = new java.util.HashMap<android.view.MenuItem, Runnable>();
        android.view.MenuItem ThisItem;
        OptionsMenu.put
          (
            TheMenu.add(R.string.show_calc_help),
            new Runnable()
              {
                public void run()
                  {
                    startActivity
                      (
                        new android.content.Intent(android.content.Intent.ACTION_VIEW)
                            .setClass(Main.this, Help.class)
                      );
                  } /*run*/
              } /*Runnable*/
          );
        ThisItem = TheMenu.add(R.string.show_overlay);
        OptionsMenu.put
          (
            ThisItem,
            new Runnable()
              {
                public void run()
                  {
                    Global.Buttons.OverlayVisible = !Global.Buttons.OverlayVisible;
                    Global.Buttons.invalidate();
                  /* ToggleOverlayItem.setChecked(Global.Buttons.OverlayVisible); */ /* apparently can't do this in initial part of options menu */
                  } /*run*/
              } /*Runnable*/
          );
      /* ThisItem.setCheckable(true); */ /* apparently can't do this in initial part of options menu */
        OptionsMenu.put
          (
            TheMenu.add(R.string.show_module_help),
            new Runnable()
              {
                public void run()
                  {
                    if (Global.Calc != null && Global.Calc.ModuleHelp != null)
                      {
                        final android.content.Intent ShowHelp =
                            new android.content.Intent(android.content.Intent.ACTION_VIEW);
                        ShowHelp.putExtra(nz.gen.geek_central.ti5x.Help.ContentID, Global.Calc.ModuleHelp);
                        ShowHelp.setClass(Main.this, Help.class);
                        startActivity(ShowHelp);
                      }
                    else
                      {
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/ getString(R.string.no_module_help),
                            /*duration =*/ android.widget.Toast.LENGTH_SHORT
                          ).show();
                      } /*if*/
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.show_printer),
            new Runnable()
              {
                public void run()
                  {
                    startActivity
                      (
                        new android.content.Intent(android.content.Intent.ACTION_VIEW)
                            .setClass(Main.this, PrinterView.class)
                      );
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.load_prog),
            new Runnable()
              {
                public void run()
                  {
                    final Picker.PickerAltList[] AltLists =
                        {
                            new Picker.PickerAltList
                              (
                                /*RadioButtonID =*/ R.id.select_saved,
                                /*Prompt =*/ getString(R.string.prog_prompt),
                                /*NoneFound =*/ getString(R.string.no_programs),
                                /*FileExt =*/ Persistent.ProgExt,
                                /*SpecialItem =*/ null
                              ),
                            new Picker.PickerAltList
                              (
                                /*RadioButtonID =*/ R.id.select_libraries,
                                /*Prompt =*/ getString(R.string.module_prompt),
                                /*NoneFound =*/ getString(R.string.no_modules),
                                /*FileExt =*/ Persistent.LibExt,
                                /*SpecialItem =*/ getString(R.string.master_library)
                                  /* item representing selection of built-in Master Library */
                              ),
                        };
                    PickerExtra = (android.view.ViewGroup)
                        getLayoutInflater().inflate(R.layout.prog_type, null);
                    Picker.Launch
                      (
                        /*Acting =*/ Main.this,
                        /*RequestCode =*/ LoadProgramRequest,
                        /*Extra =*/ PickerExtra,
                        /*LookIn =*/ Persistent.ExternalCalcDirectories,
                        /*AltLists =*/ AltLists
                      );
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.save_program_as),
            new Runnable()
              {
                public void run()
                  {
                    SaveAs.Launch
                      (
                        /*Acting =*/ Main.this,
                        /*RequestCode =*/ SaveProgramRequest,
                        /*SaveWhat =*/ getString(R.string.program),
                        /*Extra =*/ null,
                        /*FileExt =*/ Persistent.ProgExt
                      );
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.import_data),
            new Runnable()
              {
                public void run()
                  {
                    if (!Global.Calc.ImportInProgress())
                      {
                        LaunchImportPicker();
                      }
                    else
                      {
                        new ReplaceConfirm(Main.this, R.string.query_replace_import).show();
                      } /*if*/
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.export_data),
            new Runnable()
              {
                public void run()
                  {
                    if (!Global.Export.IsOpen())
                      {
                        ExportAppend = false;
                        LaunchExportPicker();
                      }
                    else
                      {
                        new ReplaceConfirm(Main.this, R.string.query_replace_export).show();
                      } /*if*/
                  } /*run*/
              } /*Runnable*/
          );
        OptionsMenu.put
          (
            TheMenu.add(R.string.turn_off),
            new Runnable()
              {
                public void run()
                  {
                    ShuttingDown = true; /* don't save any state */
                    deleteFile(Persistent.SavedStateName); /* lose any saved state */
                    finish(); /* start afresh next time */
                  } /*run*/
              } /*Runnable*/
          );
        return
            true;
      } /*onCreateOptionsMenu*/

    void BuildActivityResultActions()
      {
        ActivityResultActions = new java.util.HashMap<Integer, RequestResponseAction>();
        ActivityResultActions.put
          (
            LoadProgramRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                    final String ProgName = Data.getData().getPath();
                    final String PickedExt = Data.getStringExtra(Picker.ExtID);
                    final boolean IsLib = PickedExt.intern() == Persistent.LibExt.intern();
                    final boolean LoadingMasterLibrary = IsLib && ProgName.intern() == "/";
                  /* It appears onActivityResult is liable to be called before
                    onResume. Therefore I do additional restoring/saving state
                    here to ensure the saved state includes the newly-loaded
                    program/library. */
                    if (!StateLoaded)
                      {
                        Persistent.RestoreState(Main.this); /* if not already done */
                        StateLoaded = true;
                      } /*if*/
                    try
                      {
                        if (LoadingMasterLibrary)
                          {
                            Persistent.LoadMasterLibrary(Main.this);
                          }
                        else
                          {
                            Persistent.Load
                              (
                                /*FromFile =*/ ProgName,
                                /*Libs =*/ IsLib,
                                /*AllState =*/ false,
                                /*Disp =*/ Global.Disp,
                                /*Help =*/ Global.Help,
                                /*Buttons =*/ Global.Buttons,
                                /*Calc =*/ Global.Calc
                              );
                          } /*if*/
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/
                                String.format
                                  (
                                    Global.StdLocale,
                                    getString
                                      (
                                        IsLib ?
                                            R.string.library_loaded
                                        :
                                            R.string.program_loaded
                                      ),
                                    LoadingMasterLibrary ?
                                        getString(R.string.master_library)
                                    :
                                        new java.io.File(ProgName).getName()
                                  ),
                            /*duration =*/ android.widget.Toast.LENGTH_SHORT
                          ).show();
                      }
                    catch (Persistent.DataFormatException Failed)
                      {
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/
                                String.format
                                  (
                                    Global.StdLocale,
                                    getString(R.string.file_load_error),
                                    Failed.toString()
                                  ),
                            /*duration =*/ android.widget.Toast.LENGTH_LONG
                          ).show();
                      } /*try*/
                    Persistent.SaveState(Main.this);
                  } /*Run*/
              } /*RequestResponseAction*/
          );
        ActivityResultActions.put
          (
            SaveProgramRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                    final String TheName =
                            Data.getData().getPath().substring(1) /* ignoring leading slash */
                        +
                            Persistent.ProgExt;
                    try
                      {
                        final String SaveDir =
                                android.os.Environment.getExternalStorageDirectory()
                                    .getAbsolutePath()
                            +
                                "/"
                            +
                                Persistent.ProgramsDir;
                        new java.io.File(SaveDir).mkdirs();
                        Persistent.Save
                          (
                            /*Buttons =*/ Global.Buttons,
                            /*Calc =*/ Global.Calc,
                            /*Libs =*/ false,
                            /*AllState =*/ false,
                            /*ToFile =*/ SaveDir + "/" + TheName
                          );
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/ String.format
                              (
                                Global.StdLocale,
                                getString(R.string.program_saved),
                                TheName
                              ),
                            /*duration =*/ android.widget.Toast.LENGTH_SHORT
                          ).show();
                      }
                    catch (RuntimeException Failed)
                      {
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/
                                String.format
                                  (
                                    Global.StdLocale,
                                    getString(R.string.program_save_error),
                                    Failed.toString()
                                  ),
                            /*duration =*/ android.widget.Toast.LENGTH_LONG
                          ).show();
                      } /*try*/
                  } /*Run*/
              } /*RequestResponseAction*/
          );
        ActivityResultActions.put
          (
            ImportDataRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                    final String FileName = Data.getData().getPath();
                    try
                      {
                        Global.Calc.ClearImport();
                        Global.Import.ImportData(FileName);
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/ String.format
                              (
                                Global.StdLocale,
                                getString(R.string.import_started),
                                FileName
                              ),
                            /*duration =*/ android.widget.Toast.LENGTH_SHORT
                          ).show();
                        
                      }
                    catch (Persistent.DataFormatException Failed)
                      {
                        android.widget.Toast.makeText
                          (
                            /*context =*/ Main.this,
                            /*text =*/ String.format
                              (
                                Global.StdLocale,
                                getString(R.string.file_load_error),
                                Failed.toString()
                              ),
                            /*duration =*/ android.widget.Toast.LENGTH_LONG
                          ).show();
                      } /*try*/
                  } /*Run*/
              } /*RequestResponseAction*/
          );
        ActivityResultActions.put
          (
            ExportDataRequest,
            new RequestResponseAction()
              {
                public void Run
                  (
                    int ResultCode,
                    android.content.Intent Data
                  )
                  {
                    switch (ResultCode)
                      {
                    case android.app.Activity.RESULT_OK:
                        Global.Export.Close();
                        try
                          {
                            String FileName = Data.getData().getPath();
                            if (!ExportAppend)
                              {
                                final String SaveDir =
                                        android.os.Environment.getExternalStorageDirectory()
                                            .getAbsolutePath()
                                    +
                                        "/"
                                    +
                                        Persistent.DataDir;
                                new java.io.File(SaveDir).mkdirs();
                                FileName = SaveDir + FileName;
                                  /* note FileName will have leading slash */
                              } /*if*/
                            Global.Export.Open(FileName, ExportAppend);
                            android.widget.Toast.makeText
                              (
                                /*context =*/ Main.this,
                                /*text =*/ String.format
                                  (
                                    Global.StdLocale,
                                    getString(R.string.export_started),
                                    FileName
                                  ),
                                /*duration =*/ android.widget.Toast.LENGTH_SHORT
                              ).show();
                          }
                        catch (RuntimeException Failed)
                          {
                            android.widget.Toast.makeText
                              (
                                /*context =*/ Main.this,
                                /*text =*/
                                    String.format
                                      (
                                        Global.StdLocale,
                                        getString(R.string.export_error),
                                        Failed.toString()
                                      ),
                                /*duration =*/ android.widget.Toast.LENGTH_LONG
                              ).show();
                          } /*try*/
                    break;
                    case SwitchAppend:
                    case SwitchSaveAs:
                        ExportAppend = ResultCode == SwitchAppend;
                        LaunchExportPicker();
                    break;
                      } /*switch*/
                  } /*Run*/
              } /*RequestResponseAction*/
          );
      } /*BuildActivityResultActions*/

    @Override
    public boolean onOptionsItemSelected
      (
        android.view.MenuItem TheItem
      )
      {
        boolean Handled = false;
        final Runnable Action = OptionsMenu.get(TheItem);
        if (Action != null)
          {
            Action.run();
            Handled = true;
          } /*if*/
        return
            Handled;
      } /*onOptionsItemSelected*/

    @Override
    public void onActivityResult
      (
        int RequestCode,
        int ResultCode,
        android.content.Intent Data
      )
      {
        Picker.Cleanup();
        PickerExtra = null;
        SaveAs.Cleanup();
        SaveAsExtra = null;
        if (ResultCode != android.app.Activity.RESULT_CANCELED)
          {
            final RequestResponseAction Action = ActivityResultActions.get(RequestCode);
            if (Action != null)
              {
                Action.Run(ResultCode, Data);
              } /*if*/
          } /*if*/
      } /*onActivityResult*/

    @Override
    public void onCreate
      (
        android.os.Bundle savedInstanceState
      )
      {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Global.Disp = (Display)findViewById(R.id.display);
        Global.Help = (HelpCard)findViewById(R.id.help_card);
        Global.Buttons = (ButtonGrid)findViewById(R.id.buttons);
        Global.Print = new Printer(this);
        Global.Calc = new State(this);
        Global.Import = new Importer();
        Global.Export = new Exporter(this);
        BuildActivityResultActions();
      } /*onCreate*/

    @Override
    public void onPause()
      {
        super.onPause();
        if (!ShuttingDown)
          {
            Persistent.SaveState(this);
          } /*if*/
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        if (!StateLoaded)
          {
            Persistent.RestoreState(this);
            StateLoaded = true;
          } /*if*/
      } /*onResume*/

  } /*Main*/
