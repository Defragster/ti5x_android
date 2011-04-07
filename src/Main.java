package nz.gen.geek_central.ti5x;

public class Main extends android.app.Activity
  {
    State CurState;

  /* button functions TBD */
    class ButtonDef
      {
        final String Text, AltText;
        final int TextColor, ButtonColor;
        Runnable BaseAction, InvAction, AltAction, AltInvAction;

        public ButtonDef
          (
            String Text,
            String AltText,
            int TextColor,
            int ButtonColor
          )
          {
            this.Text = Text;
            this.AltText = AltText;
            this.TextColor = TextColor;
            this.ButtonColor = ButtonColor;
            this.BaseAction = null;
            this.InvAction = null;
            this.AltAction = null;
            this.AltInvAction = null;
          } /*ButtonDef*/
      } /*ButtonDef*/

    ButtonDef[][] ButtonDefs =
        {
            new ButtonDef[]
                {
                    new ButtonDef("A", "A´", Button.White, Button.Brown),
                    new ButtonDef("B", "B´", Button.White, Button.Brown),
                    new ButtonDef("C", "C´", Button.White, Button.Brown),
                    new ButtonDef("D", "D´", Button.White, Button.Brown),
                    new ButtonDef("E", "E´", Button.White, Button.Brown),
                },
            new ButtonDef[]
                {
                    new ButtonDef("2nd", "", Button.Black, Button.Yellow),
                    new ButtonDef("INV", "", Button.White, Button.Brown),
                    new ButtonDef("lnx", "log", Button.White, Button.Brown),
                    new ButtonDef("CE", "CP", Button.White, Button.Brown),
                    new ButtonDef("CLR", "", Button.Black, Button.Yellow),
                },
            new ButtonDef[]
                {
                    new ButtonDef("LRN", "Pgm", Button.White, Button.Brown),
                    new ButtonDef("x⇌t", "P→R", Button.White, Button.Brown),
                    new ButtonDef("x²", "sin", Button.White, Button.Brown),
                    new ButtonDef("√x", "cos", Button.White, Button.Brown),
                    new ButtonDef("1/x", "tan", Button.White, Button.Brown),
                },
            new ButtonDef[]
                {
                    new ButtonDef("SST", "Ins", Button.White, Button.Brown),
                    new ButtonDef("STO", "CMs", Button.White, Button.Brown),
                    new ButtonDef("RCL", "Exc", Button.White, Button.Brown),
                    new ButtonDef("SUM", "Prd", Button.White, Button.Brown),
                    new ButtonDef("y**x", "Ind", Button.White, Button.Brown),
                },
            new ButtonDef[]
                {
                    new ButtonDef("BST", "Del", Button.White, Button.Brown),
                    new ButtonDef("EE", "Eng", Button.White, Button.Brown),
                    new ButtonDef("(", "Fix", Button.White, Button.Brown),
                    new ButtonDef(")", "Int", Button.White, Button.Brown),
                    new ButtonDef("÷", "|x|", Button.Black, Button.Yellow),
                },
            new ButtonDef[]
                {
                    new ButtonDef("GTO", "Pause", Button.White, Button.Brown),
                    new ButtonDef("7", "x=t", Button.Black, Button.White),
                    new ButtonDef("8", "Nop", Button.Black, Button.White),
                    new ButtonDef("9", "Op", Button.Black, Button.White),
                    new ButtonDef("×", "Deg", Button.Black, Button.Yellow),
                },
            new ButtonDef[]
                {
                    new ButtonDef("SBR", "Lbl", Button.White, Button.Brown),
                    new ButtonDef("4", "x≥t", Button.Black, Button.White),
                    new ButtonDef("5", "∑x", Button.Black, Button.White),
                    new ButtonDef("6", "mean(x)", Button.Black, Button.White),
                    new ButtonDef("-", "Rad", Button.Black, Button.Yellow),
                },
            new ButtonDef[]
                {
                    new ButtonDef("RST", "St flg", Button.White, Button.Brown),
                    new ButtonDef("1", "If flg", Button.Black, Button.White),
                    new ButtonDef("2", "D.MS", Button.Black, Button.White),
                    new ButtonDef("3", "π", Button.Black, Button.White),
                    new ButtonDef("+", "Grad", Button.Black, Button.Yellow),
                },
            new ButtonDef[]
                {
                    new ButtonDef("R/S", "", Button.White, Button.Brown),
                    new ButtonDef("0", "Dsz", Button.Black, Button.White),
                    new ButtonDef(".", "Adv", Button.Black, Button.White),
                    new ButtonDef("+/-", "Prt", Button.Black, Button.White),
                    new ButtonDef("=", "List", Button.Black, Button.Yellow),
                },
        };

    void DefineActions()
      {
        ButtonDefs[1][3].BaseAction = CurState.ClearEntry();
        ButtonDefs[1][4].BaseAction = CurState.ClearAll();
        ButtonDefs[4][1].BaseAction = CurState.EnterExponent();
        ButtonDefs[5][1].BaseAction = CurState.new Digit('7');
        ButtonDefs[5][2].BaseAction = CurState.new Digit('8');
        ButtonDefs[5][3].BaseAction = CurState.new Digit('9');
        ButtonDefs[6][1].BaseAction = CurState.new Digit('4');
        ButtonDefs[6][2].BaseAction = CurState.new Digit('5');
        ButtonDefs[6][3].BaseAction = CurState.new Digit('6');
        ButtonDefs[7][1].BaseAction = CurState.new Digit('1');
        ButtonDefs[7][2].BaseAction = CurState.new Digit('2');
        ButtonDefs[7][3].BaseAction = CurState.new Digit('3');
        ButtonDefs[8][1].BaseAction = CurState.new Digit('0');
        ButtonDefs[8][2].BaseAction = CurState.DecimalPoint();
        ButtonDefs[8][3].BaseAction = CurState.ChangeSign();
      /* rest TBD -- fill in with noops for now */
        final Runnable NoOp = new Runnable()
          {
            public void run()
              {
              /* do nothing */
              } /*run*/
          };
        for (int i = 0; i < ButtonDefs.length; ++i)
          {
            final ButtonDef[] ThisRow = ButtonDefs[i];
            for (int j = 0; j < ThisRow.length; ++j)
              {
                final ButtonDef ThisButton = ThisRow[j];
                if (ThisButton.BaseAction == null)
                  {
                    ThisButton.BaseAction = NoOp;
                  } /*if*/
              } /*for*/
          } /*for*/
      } /*DefineActions*/

    @Override
    public void onCreate
      (
        android.os.Bundle savedInstanceState
      )
      {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        final android.widget.TableLayout Buttons =
            (android.widget.TableLayout)findViewById(R.id.buttons);
        Buttons.setBackgroundColor(Button.Black);
        CurState = new State((Display)findViewById(R.id.display));
        DefineActions();
        for (ButtonDef[] ThisDefRow : ButtonDefs)
          {
            final android.widget.TableRow ThisRow = new android.widget.TableRow(this);
            for (ButtonDef ThisButtonDef : ThisDefRow)
              {
                final Button ThisButton = new Button
                  (
                    this,
                    ThisButtonDef.Text,
                    ThisButtonDef.AltText,
                    ThisButtonDef.TextColor,
                    ThisButtonDef.ButtonColor,
                    ThisButtonDef.BaseAction,
                    ThisButtonDef.InvAction,
                    ThisButtonDef.AltAction,
                    ThisButtonDef.AltInvAction
                  );
                ThisRow.addView(ThisButton);
              } /*for*/
            Buttons.addView(ThisRow);
          } /*for*/
      /* more TBD */
      } /*onCreate*/

  } /*Main*/
