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
    Display Disp;
    HelpCard Help;
    ButtonGrid Buttons;
    State Calc;
    protected android.view.MenuItem ToggleOverlayItem;
    protected android.view.MenuItem ShowHelpItem;
    protected android.view.MenuItem ShowPrinterItem;
    protected android.view.MenuItem LoadProgramItem;
    protected android.view.MenuItem SaveProgramItem;
    protected android.view.MenuItem PowerOffItem;
    protected final int LoadProgramRequest = 1; /* arbitrary code */
    protected final int SaveProgramRequest = 2; /* arbitrary code */
    Boolean ShuttingDown = false;

    static final java.util.Locale StdLocale = java.util.Locale.US;

    @Override
    public boolean onCreateOptionsMenu
      (
        android.view.Menu TheMenu
      )
      {
        ToggleOverlayItem = TheMenu.add(R.string.show_overlay);
        ToggleOverlayItem.setCheckable(true);
        ShowHelpItem = TheMenu.add(R.string.show_help);
        ShowPrinterItem = TheMenu.add(R.string.show_printer);
        LoadProgramItem = TheMenu.add(getString(R.string.load_prog));
        SaveProgramItem = TheMenu.add(getString(R.string.save_as));
        PowerOffItem = TheMenu.add(R.string.turn_off);
        return
            true;
      } /*onCreateOptionsMenu*/

    @Override
    public boolean onOptionsItemSelected
      (
        android.view.MenuItem TheItem
      )
      {
        boolean Handled = false;
        if (TheItem == ToggleOverlayItem)
          {
            Buttons.OverlayVisible = !Buttons.OverlayVisible;
            Buttons.invalidate();
            ToggleOverlayItem.setChecked(Buttons.OverlayVisible); /* doesn't seem to work */
          }
        else if (TheItem == ShowHelpItem)
          {
            startActivity
              (
                new android.content.Intent(android.content.Intent.ACTION_VIEW)
                    .setClass(this, Help.class)
              );
          }
        else if (TheItem == ShowPrinterItem)
          {
            startActivity
              (
                new android.content.Intent(android.content.Intent.ACTION_VIEW)
                    .setClass(this, PrinterView.class)
              );
          }
        else if (TheItem == LoadProgramItem)
          {
            startActivityForResult
              (
                new android.content.Intent(android.content.Intent.ACTION_PICK)
                    .setClass(this, Picker.class),
                LoadProgramRequest
              );
          }
        else if (TheItem == SaveProgramItem)
          {
            startActivityForResult
              (
                new android.content.Intent(android.content.Intent.ACTION_PICK)
                    .setClass(this, SaveAs.class),
                SaveProgramRequest
              );
          }
        else if (TheItem == PowerOffItem)
          {
            ShuttingDown = true;
            deleteFile(Persistent.SavedStateName); /* lose any saved state */
            finish(); /* start afresh next time */
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
        if
          (
                RequestCode == LoadProgramRequest
            &&
                Data != null
          )
          {
            final String ProgName = Data.getData().getPath();
            final String PickedExt = Data.getStringExtra(Picker.ExtID);
            final boolean IsLib = PickedExt == Persistent.LibExt;
          /* It appears onActivityResult is liable to be called before
            onResume. Therefore I do additional restoring/saving state
            here to ensure the saved state includes the newly-loaded
            program/library. */
            Persistent.RestoreState(this, Disp, Help, Buttons, Calc); /* if not already done */
            try
              {
                Persistent.Load
                  (
                    /*FromFile =*/ ProgName,
                    /*Libs =*/ IsLib,
                    /*AllState =*/ false,
                    /*Disp =*/ Disp,
                    /*Help =*/ Help,
                    /*Buttons =*/ Buttons,
                    /*Calc =*/ Calc
                  );
                android.widget.Toast.makeText
                  (
                    /*context =*/ this,
                    /*text =*/
                        String.format
                          (
                            StdLocale,
                            getString
                              (
                                IsLib ?
                                    R.string.library_loaded
                                :
                                    R.string.program_loaded
                              ),
                              new java.io.File(ProgName).getName()
                          ),
                    /*duration =*/ android.widget.Toast.LENGTH_SHORT
                  ).show();
              }
            catch (Persistent.DataFormatException Failed)
              {
                android.widget.Toast.makeText
                  (
                    /*context =*/ this,
                    /*text =*/
                        String.format
                          (
                            StdLocale,
                            getString(R.string.file_load_error),
                            Failed.toString()
                          ),
                    /*duration =*/ android.widget.Toast.LENGTH_LONG
                  ).show();
              } /*try*/
            Persistent.SaveState(this, Buttons, Calc);
          }
        else if
          (
                RequestCode == SaveProgramRequest
            &&
                Data != null
          )
          {
            final String TheName =
                    Data.getData().getPath().substring(1) /* ignoring leading slash */
                +
                    Persistent.ProgExt;
            try
              {
                final String SaveDir =
                        android.os.Environment.getExternalStorageDirectory().getAbsolutePath()
                    +
                        "/"
                    +
                        Persistent.ProgramsDir;
                new java.io.File(SaveDir).mkdirs();
                Persistent.Save
                  (
                    /*Buttons =*/ Buttons,
                    /*Calc =*/ Calc,
                    /*Libs =*/ false,
                    /*AllState =*/ false,
                    /*ToFile =*/ SaveDir + "/" + TheName
                  );
                android.widget.Toast.makeText
                  (
                    /*context =*/ this,
                    /*text =*/ String.format(StdLocale, getString(R.string.program_saved), TheName),
                    /*duration =*/ android.widget.Toast.LENGTH_SHORT
                  ).show();
              }
            catch (RuntimeException Failed)
              {
                android.widget.Toast.makeText
                  (
                    /*context =*/ this,
                    /*text =*/
                        String.format(StdLocale, getString(R.string.program_save_error), Failed.toString()),
                    /*duration =*/ android.widget.Toast.LENGTH_LONG
                  ).show();
              } /*try*/
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
        Disp = (Display)findViewById(R.id.display);
        Help = (HelpCard)findViewById(R.id.help_card);
        Buttons = (ButtonGrid)findViewById(R.id.buttons);
        final Printer Print = new Printer();
        Calc = new State(Disp, Help, Print);
        Buttons.Calc = Calc;
      } /*onCreate*/

    @Override
    public void onPause()
      {
        super.onPause();
        if (!ShuttingDown)
          {
            Persistent.SaveState(this, Buttons, Calc);
          } /*if*/
      } /*onPause*/

    @Override
    public void onResume()
      {
        super.onResume();
        Persistent.RestoreState(this, Disp, Help, Buttons, Calc);
      } /*onResume*/

  } /*Main*/
