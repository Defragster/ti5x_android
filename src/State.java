package nz.gen.geek_central.ti5x;

public class State
  /* the calculation state and number entry */
  {
    final static int EntryState = 0;
    final static int DecimalEntryState = 1;
    final static int ExponentEntryState = 2;
    final static int ResultState = 10;
    final static int ErrorState = 11;
    int CurState = EntryState;
    boolean ExponentEntered = false;

    Display TheDisplay;
    String CurDisplay;

    public static final int FORMAT_FIXED = 0;
    public static final int FORMAT_FLOATING = 1;
    public static final int FORMAT_ENG = 2;
    int CurFormat = FORMAT_FIXED;
    int CurNrDecimals = -1;

    public static final int ANG_RAD = 1;
    public static final int ANG_DEG = 2;
    public static final int ANG_GRAD = 3;
    int CurAng = ANG_DEG;

    public final static int STACKOP_ADD = 1;
    public final static int STACKOP_SUB = 2;
    public final static int STACKOP_MUL = 3;
    public final static int STACKOP_DIV = 4;
    public final static int STACKOP_EXP = 5;
    public final static int STACKOP_ROOT = 6;

    class StackEntry
      {
        double Operand;
        int Operator;
        int ParenFollows;

        public StackEntry
          (
            double Operand,
            int Operator
          )
          {
            this.Operand = Operand;
            this.Operator = Operator;
            ParenFollows = 0;
          } /*StackEntry*/

      } /*StackEntry*/

    final int MaxStack = 8;
    double X, T;
    StackEntry Stack[];
    int StackNext;

    public boolean ProgMode = false;
    public final int MaxMemories = 30; /* TBD make this configurable */ /* can't be zero */
    public final int MaxProgram = 460; /* TBD make this configurable */
    public final int MaxFlags = 10;
    final double[] Memory;
    final byte[] Program;
    final boolean[] Flag;
    int PC;
    public boolean ProgRunning = false;
    public boolean ProgRunningSlowly = false;
    String LastShowing = null;

    public State
      (
        Display TheDisplay
      )
      {
        this.TheDisplay = TheDisplay;
        Stack = new StackEntry[MaxStack];
        StackNext = 0;
        X = 0.0;
        T = 0.0;
        Memory = new double[MaxMemories];
        Program = new byte[MaxProgram];
        Flag = new boolean[MaxFlags];
        PC = 0;
        ResetEntry();
      } /*State*/

    void SetShowing
      (
        String ToDisplay
      )
      {
        LastShowing = ToDisplay;
        if (!ProgRunning)
          {
            TheDisplay.SetShowing(ToDisplay);
          }
      } /*SetShowing*/

    public void ResetEntry()
      {
        CurState = EntryState;
        if (CurFormat == FORMAT_FIXED)
          {
            CurDisplay = "0.";
            ExponentEntered = false;
          }
        else
          {
            CurDisplay = "0. 00";
            ExponentEntered = true;
          } /*if*/
        SetShowing(CurDisplay);
      } /*ResetEntry*/

    public void Enter()
      /* finishes the entry of the current number. */
      {
        if (CurState != ResultState && CurState != ErrorState)
          {
            int Exp;
            if (ExponentEntered)
              {
                Exp = Integer.parseInt(CurDisplay.substring(CurDisplay.length() - 2));
                if (CurDisplay.charAt(CurDisplay.length() - 3) == '-')
                  {
                    Exp = - Exp;
                  } /*if*/
              }
            else
              {
                Exp = 0;
              } /*if*/
            X = Double.parseDouble
              (
                CurDisplay.substring
                  (
                    0,
                    ExponentEntered ? CurDisplay.length() - 3 : CurDisplay.length()
                  )
              );
            if (ExponentEntered)
              {
                X = X * Math.pow(10.0, Exp);
              } /*if*/
            SetX(X);
          } /*if*/
      } /*Enter*/

    public void SetErrorState()
      {
        if (!ProgRunning)
          {
            TheDisplay.SetShowingError();
          } /*if*/
        CurState = ErrorState;
      } /*SetErrorState*/

    public boolean InErrorState()
      {
        return
            CurState == ErrorState;
      } /*InErrorState*/

    public void ClearAll()
      {
        StackNext = 0;
        ResetEntry();
      } /*ClearAll*/

    public void ClearEntry()
      {
        if (CurState != ResultState)
          {
            ResetEntry();
          } /*if*/
      } /*ClearEntry*/

    public void Digit
      (
        char TheDigit
      )
      {
        if (CurState == ResultState)
          {
            ResetEntry();
          } /*if*/
        String SaveExponent = "";
        switch (CurState)
          {
        case EntryState:
        case DecimalEntryState:
            if (ExponentEntered)
              {
                SaveExponent = CurDisplay.substring(CurDisplay.length() - 3);
                CurDisplay = CurDisplay.substring(0, CurDisplay.length() - 3);
              } /*if*/
        break;
          } /*switch*/
        switch (CurState)
          {
        case EntryState:
            if (CurDisplay.charAt(0) == '0')
              {
                CurDisplay = new String(new char[] {TheDigit}) + CurDisplay.substring(1);
              }
            else if (CurDisplay.charAt(0) == '-' && CurDisplay.charAt(1) == '0')
              {
                CurDisplay = "-" + new String(new char[] {TheDigit}) + CurDisplay.substring(2);
              }
            else
              {
                CurDisplay =
                        CurDisplay.substring(0,CurDisplay.length() - 1)
                    +
                        new String(new char[] {TheDigit})
                    +
                        CurDisplay.substring(CurDisplay.length() - 1);
              } /*if*/
        break;
        case DecimalEntryState:
            CurDisplay = CurDisplay + new String(new char[] {TheDigit});
        break;
        case ExponentEntryState:
          /* old exponent units digit becomes tens digit, new digit
            becomes units digit */
            CurDisplay =
                    CurDisplay.substring(0, CurDisplay.length() - 2)
                +
                    CurDisplay.substring(CurDisplay.length() - 1)
                +
                    new String(new char[] {TheDigit});
        break;
          } /*switch*/
        CurDisplay += SaveExponent;
        if (CurState != ErrorState)
          {
            SetShowing(CurDisplay);
          } /*if*/
      } /*Digit*/

    public void DecimalPoint()
      {
        if (CurState == ResultState)
          {
            ResetEntry();
          } /*if*/
        switch (CurState)
          {
        case EntryState:
        case ExponentEntryState:
            CurState = DecimalEntryState;
        break;
      /* otherwise ignore */
          } /*CurState*/
      } /*DecimalPoint*/

    public void EnterExponent
      (
        boolean InvState /* TBD */
      )
      {
        switch (CurState)
          {
        case EntryState:
        case DecimalEntryState:
            if (!ExponentEntered)
              {
                CurDisplay = CurDisplay + " 00";
              } /*if*/
            CurState = ExponentEntryState;
            SetShowing(CurDisplay);
            ExponentEntered = true;
        break;
        case ResultState:
            if (CurFormat != FORMAT_FLOATING)
              {
                CurFormat = FORMAT_FLOATING;
                SetX(X); /* will cause redisplay */
              } /*if*/
        break;
          } /*switch*/
      } /*EnterExponent*/

    public void SetX
      (
        double NewX
      )
      /* sets the display to show the specified value. */
      {
        int UseFormat = CurFormat;
        int Exp, BeforeDecimal;
        CurState = ResultState;
        X = NewX;
        if (!Double.isNaN(X) && !Double.isInfinite(X))
          {
            if
              (
                    CurFormat == FORMAT_FIXED
                &&
                    X != 0
                &&
                    (Math.abs(X) < 5.0 * Math.pow(10.0, -11.0) || Math.abs(X) > Math.pow(10.0, 10.0))
              )
              {
                UseFormat = FORMAT_FLOATING;
              } /*if*/
            Exp = 0;
            if (X != 0.0)
              {
                switch (UseFormat)
                  {
                case FORMAT_FLOATING:
                    Exp = (int)Math.floor(Math.log(Math.abs(X)) / Math.log(10.0));
                break;
                case FORMAT_ENG:
                    Exp = (int)Math.floor(Math.log(Math.abs(X)) / Math.log(1000.0)) * 3;
                break;
                  } /*switch*/
              } /*if*/
            if (X != 0.0)
              {
                BeforeDecimal = Math.max((int)Math.floor(Math.log10(X / Math.pow(10.0, Exp))), 1);
              }
            else
              {
                BeforeDecimal = 1;
              } /*if*/
            switch (UseFormat)
              {
            case FORMAT_FLOATING:
            case FORMAT_ENG:
                CurDisplay = String.format
                  (
                    java.util.Locale.US,
                    String.format(java.util.Locale.US, "%%.%df", Math.max(8 - BeforeDecimal, 0)),
                    X / Math.pow(10.0, Exp)
                  );
            break;
            case FORMAT_FIXED:
                if (CurNrDecimals >= 0)
                  {
                    CurDisplay = String.format
                      (
                        java.util.Locale.US,
                        String.format(java.util.Locale.US, "%%.%df", Math.max(CurNrDecimals + 1 - BeforeDecimal, 0)),
                        X / Math.pow(10.0, Exp)
                      );
                  }
                else
                  {
                    CurDisplay = String.format
                      (
                        java.util.Locale.US,
                        String.format(java.util.Locale.US, "%%.%df", Math.max(11 - BeforeDecimal, 0)),
                        X / Math.pow(10.0, Exp)
                      );
                    while
                      (
                            CurDisplay.length() != 0
                        &&
                            CurDisplay.charAt(CurDisplay.length() - 1) == '0'
                      )
                      {
                        CurDisplay = CurDisplay.substring(0, CurDisplay.length() - 1);
                      } /*while*/
                  } /*if*/
            break;
              } /*switch*/
            if (CurDisplay.length() == 0)
              {
                CurDisplay = "0.";
              } /*if*/
          /* assume there will always be a decimal point? */
            if (UseFormat != FORMAT_FIXED)
              {
                CurDisplay += (Exp < 0 ? "-" : " ") + String.format(java.util.Locale.US, "%02d", Math.abs(Exp));
              } /*if*/
            SetShowing(CurDisplay);
          }
        else
          {
            SetErrorState();
          } /*if*/
      } /*SetX*/

    public void ChangeSign()
      {
        switch (CurState)
          {
        case EntryState:
        case DecimalEntryState:
            if (CurDisplay.charAt(0) == '-')
              {
                CurDisplay = CurDisplay.substring(1);
              }
            else
              {
                CurDisplay = "-" + CurDisplay;
              } /*if*/
            SetShowing(CurDisplay);
        break;
        case ExponentEntryState:
            CurDisplay =
                    CurDisplay.substring(0, CurDisplay.length() - 3)
                +
                    (CurDisplay.charAt(CurDisplay.length() - 3) == '-' ? ' ' : '-')
                +
                    CurDisplay.substring(CurDisplay.length() - 2);
            SetShowing(CurDisplay);
        break;
        case ResultState:
            SetX(- X);
        break;
          } /*switch*/
      } /*ChangeSign*/

    public void SetDisplayMode
      (
        int NewMode,
        int NewNrDecimals
      )
      {
        Enter();
        CurFormat = NewMode;
        CurNrDecimals = NewNrDecimals;
        SetX(X);
      } /*SetDisplayMode*/

    void DoStackTop()
      {
        final StackEntry ThisOp = Stack[--StackNext];
        switch (ThisOp.Operator)
          {
        case STACKOP_ADD:
            X = ThisOp.Operand + X;
        break;
        case STACKOP_SUB:
            X = ThisOp.Operand - X;
        break;
        case STACKOP_MUL:
            X = ThisOp.Operand * X;
        break;
        case STACKOP_DIV:
            X = ThisOp.Operand / X;
        break;
        case STACKOP_EXP:
            X = Math.pow(ThisOp.Operand, X);
        break;
        case STACKOP_ROOT:
            X = Math.pow(ThisOp.Operand, 1.0 / X);
        break;
          } /*switch*/
      /* leave it to caller to update display */
      } /*DoStackTop*/

    static int Precedence
      (
        int OpCode
      )
      {
        int Result = -1;
        switch (OpCode)
          {
        case STACKOP_ADD:
        case STACKOP_SUB:
            Result = 1;
        break;
        case STACKOP_MUL:
        case STACKOP_DIV:
            Result = 2;
        break;
        case STACKOP_EXP:
        case STACKOP_ROOT:
            Result = 3;
        break;
          } /*switch*/
        return
            Result;
      } /*Precedence*/

    void StackPush
      (
        int OpCode
      )
      {
        if (StackNext == MaxStack)
          {
          /* overflow! lose bottom of stack */
            for (int i = 0; i < MaxStack; ++i)
              {
                Stack[i - 1] = Stack[i];
              } /*for*/
            --StackNext;
          } /*if*/
        Stack[StackNext++] = new StackEntry(X, OpCode);
      } /*StackPush*/

    public void Operator
      (
        int OpCode
      )
      {
        Enter();
        boolean PoppedSomething = false;
        for (;;)
          {
            if
              (
                    StackNext == 0
                ||
                    Stack[StackNext - 1].ParenFollows != 0
                ||
                    Precedence(Stack[StackNext - 1].Operator) < Precedence(OpCode)
              )
                break;
            DoStackTop();
            PoppedSomething = true;
          } /*for*/
        if (PoppedSomething)
          {
            SetX(X);
          } /*if*/
        StackPush(OpCode);
      } /*Operator*/

    public void LParen()
      {
        Enter();
        if (StackNext != 0)
          {
            ++Stack[StackNext - 1].ParenFollows;
          } /*if*/
      /* else ignored */
      } /*LParen*/

    public void RParen()
      {
        Enter();
        boolean PoppedSomething = false;
        for (;;)
          {
            if (StackNext == 0)
                break;
            if (Stack[StackNext - 1].ParenFollows != 0)
              {
                --Stack[StackNext - 1].ParenFollows;
                break;
              } /*if*/
            DoStackTop();
            PoppedSomething = true;
          } /*for*/
        if (PoppedSomething)
          {
            SetX(X);
          } /*if*/
      } /*RParen*/

    public void Equals()
      {
        Enter();
        while (StackNext != 0)
          {
            DoStackTop();
          } /*while*/
        SetX(X);
      } /*Equals*/

    public void SetAngMode
      (
        int NewMode
      )
      {
        CurAng = NewMode;
      } /*SetAngMode*/

    public void Square()
      {
        Enter();
        SetX(X * X);
      } /*Square*/

    public void Sqrt()
      {
        Enter();
        SetX(Math.sqrt(X));
      } /*Sqrt*/

    public void Reciprocal()
      {
        Enter();
        SetX(1.0 / X);
      } /*Reciprocal*/

    double TrigScale()
      {
        Double Scale = 0.0;
        switch (CurAng)
          {
        case ANG_RAD:
            Scale = 1.0;
        break;
        case ANG_DEG:
            Scale = 180.0 / Math.PI;
        break;
        case ANG_GRAD:
            Scale = 200.0 / Math.PI;
        break;
          } /*CurAng*/
        return
            Scale;
      } /*TrigScale*/

    public void Sin
      (
        boolean InvState
      )
      {
        Enter();
        if (InvState)
          {
            SetX(Math.asin(X) * TrigScale());
          }
        else
          {
            SetX(Math.sin(X / TrigScale()));
          } /*if*/
      } /*Sin*/

    public void Cos
      (
        boolean InvState
      )
      {
        Enter();
        if (InvState)
          {
            SetX(Math.acos(X) * TrigScale());
          }
        else
          {
            SetX(Math.cos(X / TrigScale()));
          } /*if*/
      } /*Cos*/

    public void Tan
      (
        boolean InvState
      )
      {
        Enter();
        if (InvState)
          {
            SetX(Math.atan(X) * TrigScale());
          }
        else
          {
            SetX(Math.tan(X / TrigScale()));
          } /*if*/
      } /*Tan*/

    public void Ln
      (
        boolean InvState
      )
      {
        Enter();
        if (InvState)
          {
            SetX(Math.exp(X));
          }
        else
          {
            SetX(Math.log(X));
          } /*if*/
      } /*Ln*/

    public void Log
      (
        boolean InvState
      )
      {
        Enter();
        if (InvState)
          {
            SetX(Math.pow(10.0, X));
          }
        else
          {
            SetX(Math.log10(X));
          } /*if*/
      } /*Log*/

    public void Pi()
      {
        SetX(Math.PI);
      } /*Pi*/

    public void Int
      (
        boolean InvState
      )
      {
        Enter();
        final Double IntPart = Math.floor(Math.abs(X));
        if (InvState)
          {
            SetX((Math.abs(X) - IntPart) * Math.signum(X));
          }
        else
          {
            SetX(IntPart * Math.signum(X));
          } /*if*/
      } /*Int*/

    public void Abs()
      {
        SetX(Math.abs(X));
      } /*Abs*/

    public void SwapT()
      {
        Enter();
        final double SwapTemp = X;
        SetX(T);
        T = SwapTemp;
      } /*SwapT*/

    public void Polar
      (
        boolean InvState
      )
      {
        Enter();
        final Double Scale = TrigScale();
        Double NewX, NewY;
        if (InvState)
          {
            NewX = Math.sqrt(X * X + T * T);
            NewY = Math.atan2(X, T) * Scale;
          }
        else
          {
            NewX = T * Math.cos(X / Scale);
            NewY = T * Math.sin(X / Scale);
          } /*if*/
        T = NewX;
        SetX(NewY);
      } /*Polar*/

    public void D_MS
      (
        boolean InvState
      )
      {
        Enter();
        final Double Sign = Math.signum(X);
        final Double Degrees = Math.floor(Math.abs(X));
        final Double Fraction = Math.abs(X) - Degrees;
        if (InvState)
          {
            final double Minutes = Math.floor(Fraction * 60.0);
            SetX((Degrees + Minutes / 100.0 + (Fraction * 60.0 - Minutes) * 6 / 1000.0) * Sign);
          }
        else
          {
            final Double Minutes = Math.floor(Fraction * 100.0);
            SetX((Degrees + Minutes / 60.0 + (Fraction * 100.0 - Minutes) / 36.0) * Sign);
          } /*if*/
      } /*D_MS*/

    void ShowCurProg()
      {
        SetShowing(String.format(java.util.Locale.US, "%03d %02d", PC, (int)Program[PC]));
      } /*ShowCurProg*/

    public void SetProgMode
      (
        boolean NewProgMode
      )
      {
        ProgMode = NewProgMode;
        if (ProgMode)
          {
            ShowCurProg();
          }
        else
          {
            SetShowing(CurDisplay);
          } /*if*/
      } /*SetProgMode*/

    public void ClearMemories()
      {
        Enter(); /*?*/
        for (int i = 0; i < MaxMemories; ++i)
          {
            Memory[i] = 0.0;
          } /*for*/
      } /*ClearMemories*/

    public void ClearProgram()
      {
        Enter(); /*?*/
        for (int i = 0; i < MaxProgram; ++i)
          {
            Program[i] = (byte)0;
          } /*if*/
        PC = 0;
        T = 0.0;
        for (int i = 0; i < MaxFlags; ++i)
          {
            Flag[i] = false;
          } /*for*/
      } /*ClearProgram*/

    public void SelectProgram
      (
        int ProgNr
      )
      {
      /* TBD */
      } /*SelectProgram*/

    public static final int MEMOP_STO = 1;
    public static final int MEMOP_RCL = 2;
    public static final int MEMOP_ADD = 3;
    public static final int MEMOP_SUB = 4;
    public static final int MEMOP_MUL = 5;
    public static final int MEMOP_DIV = 6;
    public static final int MEMOP_EXC = 7;

    public void MemoryOp
      (
        int Op,
        int RegNr,
        boolean Indirect
      )
      {
        Enter();
        boolean OK = false; /* to begin with */
        do /*once*/
          {
            if (RegNr < 0 || RegNr >= MaxMemories)
                break;
            if (Indirect)
              {
                RegNr = (int)Math.round(Memory[RegNr]);
                if (RegNr < 0 || RegNr >= MaxMemories)
                    break;
              } /*if*/
            switch (Op)
              {
            case MEMOP_STO:
                Memory[RegNr] = X;
            break;
            case MEMOP_RCL:
                SetX(Memory[RegNr]);
            break;
            case MEMOP_ADD:
                Memory[RegNr] += X;
            break;
            case MEMOP_SUB:
                Memory[RegNr] -= X;
            break;
            case MEMOP_MUL:
                Memory[RegNr] *= X;
            break;
            case MEMOP_DIV:
                Memory[RegNr] /= X;
            break;
            case MEMOP_EXC:
                final double Temp = Memory[RegNr];
                Memory[RegNr] = X;
                SetX(Temp);
            break;
              } /*switch*/
          /* all done */
            OK = true;
          }
        while (false);
        if (!OK)
          {
            SetErrorState();
          } /*if*/
      } /*MemoryOp*/

    public void Nop()
      {
        Enter();
      } /*Nop*/

    public void SpecialOp
      (
        int OpNr,
        boolean Indirect
      )
      {
        Enter();
        boolean OK = false;
        do /*once*/
          {
            if (Indirect)
              {
                if (OpNr >= MaxMemories)
                    break;
                OpNr = (int)Math.round(Memory[OpNr]);
              } /*if*/
            if (OpNr >= 20 && OpNr < 30)
              {
                Memory[OpNr - 20] += 1.0;
                OK = true;
                break;
              } /*if*/
            if (OpNr >= 30 && OpNr < 40)
              {
                Memory[OpNr - 30] -= 1.0;
                OK = true;
                break;
              } /*if*/
            switch (OpNr)
              {
          /* more TBD */
            case 10:
                SetX(Math.signum(X));
                OK = true;
            break;
            case 11:
              /* sample variance */
                T = Memory[5] / Memory[3] - Memory[4] * Memory[4] / (Memory[3] * Memory[3]);
                SetX(Memory[2] / Memory[3] - Memory[1] * Memory[1] / (Memory[3] * Memory[3]));
                OK = true;
            break;
            case 12:
              /* slope and intercept */
                T =
                        (Memory[6] - Memory[1] * Memory[4] / Memory[3])
                    /
                        (Memory[5] - Memory[4] * Memory[4] / Memory[3]);
                SetX
                  (
                    (Memory[1] - T * Memory[4]) / Memory[3]
                  );
                OK = true;
            break;
            case 13:
              /* correlation coefficient */
                SetX
                  (
                        (Memory[6] - Memory[1] * Memory[4] / Memory[3])
                    /
                        (Memory[5] - Memory[4] * Memory[4] / Memory[3])
                    *
                        Math.sqrt
                          (
                            (Memory[5] - Memory[4] * Memory[4] / Memory[3]) / (Memory[3] - 1.0)
                          )
                    /
                        Math.sqrt
                          (
                            (Memory[2] - Memory[1] * Memory[1] / Memory[3]) / (Memory[3] - 1.0)
                          )
                  );
                OK = true;
            break;
            case 14:
              /* estimated y from x */
                  {
                    final double m =
                            (Memory[6] - Memory[1] * Memory[4] / Memory[3])
                        /
                            (Memory[5] - Memory[4] * Memory[4] / Memory[3]);
                    SetX
                      (
                            m * X
                        +
                            (Memory[1] - m * Memory[4]) / Memory[3]
                      );
                  }
                OK = true;
            break;
            case 15:
              /* estimated x from y */
                  {
                    final double m =
                            (Memory[6] - Memory[1] * Memory[4] / Memory[3])
                        /
                            (Memory[5] - Memory[4] * Memory[4] / Memory[3]);
                    SetX
                      (
                            ((Memory[1] - m * Memory[4]) / Memory[3] - X)
                        /
                            m
                      );
                  }
                OK = true;
            break;
            case 16:
                SetX(MaxProgram - 1.0 + (MaxMemories - 1.0) / 100.0);
                OK = true;
            break;
          /* more TBD */
          /* 20-39 handled above */
              } /*switch*/
          }
        while (false);
        if (!OK)
          {
            SetErrorState();
          } /*if*/
      } /*SpecialOp*/

    public void StatsSum
      (
        boolean InvState
      )
      {
        Enter();
        if (InvState)
          {
          /* remove sample */
            Memory[1] -= X;
            Memory[2] -= X * X;
            Memory[3] -= 1.0;
            Memory[4] -= T;
            Memory[5] -= T * T;
            Memory[6] -= X * T;
            T -= 1.0;
          }
        else
          {
          /* accumulate sample */
            Memory[1] += X;
            Memory[2] += X * X;
            Memory[3] += 1.0;
            Memory[4] += T;
            Memory[5] += T * T;
            Memory[6] += X * T;
            T += 1.0;
          } /*if*/
        SetX(Memory[3]);
      } /*StatsSum*/

    public void StatsResult
      (
        boolean InvState
      )
      {
        if (InvState)
          {
          /* population standard deviation */
            T = Math.sqrt((Memory[5] - Memory[4] * Memory[4] / Memory[3]) / (Memory[3] - 1.0));
            SetX
              (
                Math.sqrt((Memory[2] - Memory[1] * Memory[1] / Memory[3]) / (Memory[3] - 1.0))
              );
          }
        else
          {
          /* sample mean */
            T = Memory[4] / Memory[3];
            SetX(Memory[1] / Memory[3]);
          } /*if*/
      } /*StatsResult*/

    public void StepPC
      (
        boolean Forward
      )
      {
        if (Forward)
          {
            if (PC < MaxProgram - 1)
              {
                ++PC;
                ShowCurProg();
              } /*if*/
          }
        else
          {
            if (PC > 0)
              {
                --PC;
                ShowCurProg();
              } /*if*/
          } /*if*/
      } /*StepPC*/

    public void StoreInstr
      (
        int Instr
      )
      {
      /* TBD instruction merging */
        Program[PC] = (byte)Instr;
        if (PC < MaxProgram - 1)
          {
            ++PC;
            ShowCurProg();
          }
        else
          {
            SetErrorState();
          } /*if*/
      } /*StoreInstr*/

    public void InsertAtCurInstr()
      {
        for (int i = MaxProgram; i > PC + 1; --i)
          {
            Program[i - 1] = Program[i - 2];
          } /*for*/
        Program[PC] = (byte)0;
        ShowCurProg();
      } /*InsertAtCurInstr*/

    public void DeleteCurInstr()
      {
        for (int i = PC; i < MaxProgram - 1; ++i)
          {
            Program[i] = Program[i + 1];
          } /*for*/
        Program[MaxProgram - 1] = (byte)0;
        ShowCurProg();
      } /*DeleteCurInstr*/

    public void StepProgram()
      {
      /* TBD */
      } /*StepProgram*/

    public void StartProgram()
      {
      /* TBD */
        ProgRunningSlowly = false; /* just in case */
        ProgRunning = true;
        TheDisplay.SetShowingRunning();
      } /*StartProgram*/

    public void StopProgram()
      {
      /* TBD */
        ProgRunning = false;
        if (CurState == ErrorState)
          {
            TheDisplay.SetShowingError();
          }
        else
          {
            TheDisplay.SetShowing(LastShowing);
          } /*if*/
      } /*StopProgram*/

    public void SetSlowExecution
      (
        boolean Slow
      )
      {
        ProgRunningSlowly = Slow;
      /* more TBD? */
      } /*SetSlowExecution*/

    public void Reset()
      {
        for (int i = 0; i < MaxFlags; ++i)
          {
            Flag[i] = false;
          } /*for*/
        PC = 0;
      } /*Reset*/

  /* more TBD */

  } /*State*/
