package nz.gen.geek_central.ti5x;
/*
    ti5x calculator emulator -- virtual printer rendering

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

public class Printer
  {
    public android.graphics.Bitmap Paper;
      /* the idea is that this is low-resolution but will be displayed scaled up
        to make matrix dots more visible */
    public android.view.View ShowingView; /* to invalidate when Paper changes */
    android.graphics.Canvas PaperDraw;

    final static int DotSize = 2;
    final static int DotGap = 1;
    public final static int CharColumns = 20;
    final static int CharWidth = 5;
    final static int CharHeight = 7;
    final static int CharHorGap = 1;
    final static int CharVertGap = 1;
    final static int CharLines = 25; /* perhaps make this configurable? */

    final int PaperWidth = (CharColumns * CharWidth + (CharColumns + 1) * CharHorGap) * (DotSize + DotGap) + DotGap;
    final int PaperHeight = (CharLines * CharHeight + (CharLines + 1) * CharVertGap) * (DotSize + DotGap) + DotGap;
    final int LineHeight = (CharHeight + CharVertGap) * (DotSize + DotGap);

    final int PaperColor;
    final int InkColor;

    static final int[][] Chars =
        {
            {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, /*00*/
            {0x0e, 0x11, 0x11, 0x11, 0x11, 0x11, 0x0e}, /*01*/
            {0x04, 0x06, 0x04, 0x04, 0x04, 0x04, 0x0e}, /*02*/
            {0x0e, 0x11, 0x10, 0x0c, 0x02, 0x01, 0x1f}, /*03*/
            {0x0e, 0x11, 0x10, 0x0c, 0x10, 0x11, 0x0e}, /*04*/
            {0x08, 0x0c, 0x0a, 0x09, 0x1f, 0x08, 0x08}, /*05*/
            {0x1f, 0x01, 0x0f, 0x10, 0x10, 0x11, 0x0e}, /*06*/
            {0x0c, 0x02, 0x01, 0x0f, 0x11, 0x11, 0x0e}, /*07*/
            {0x1f, 0x10, 0x08, 0x04, 0x02, 0x02, 0x02}, /*10*/
            {0x0e, 0x11, 0x11, 0x0e, 0x11, 0x11, 0x0e}, /*11*/
            {0x0e, 0x11, 0x11, 0x1e, 0x10, 0x08, 0x06}, /*12*/
            {0x0e, 0x11, 0x11, 0x1f, 0x11, 0x11, 0x11}, /*13*/
            {0x0f, 0x11, 0x11, 0x0f, 0x11, 0x11, 0x0f}, /*14*/
            {0x0e, 0x11, 0x01, 0x01, 0x01, 0x11, 0x0e}, /*15*/
            {0x0f, 0x12, 0x12, 0x12, 0x12, 0x12, 0x0f}, /*16*/
            {0x1f, 0x01, 0x01, 0x0f, 0x01, 0x01, 0x1f}, /*17*/
            {0x00, 0x00, 0x00, 0x1f, 0x00, 0x00, 0x00}, /*20*/
            {0x1f, 0x01, 0x01, 0x0f, 0x01, 0x01, 0x01}, /*21*/
            {0x0e, 0x11, 0x01, 0x01, 0x19, 0x11, 0x1e}, /*22*/
            {0x11, 0x11, 0x11, 0x1f, 0x11, 0x11, 0x11}, /*23*/
            {0x0e, 0x04, 0x04, 0x04, 0x04, 0x04, 0x0e}, /*24*/
            {0x10, 0x10, 0x10, 0x10, 0x10, 0x11, 0x0e}, /*25*/
            {0x11, 0x09, 0x05, 0x03, 0x05, 0x09, 0x11}, /*26*/
            {0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x1f}, /*27*/
            {0x11, 0x1b, 0x15, 0x15, 0x11, 0x11, 0x11}, /*30*/
            {0x11, 0x11, 0x13, 0x15, 0x19, 0x11, 0x11}, /*31*/
            {0x1f, 0x11, 0x11, 0x11, 0x11, 0x11, 0x1f}, /*32*/
            {0x0f, 0x11, 0x11, 0x0f, 0x01, 0x01, 0x01}, /*33*/
            {0x0e, 0x11, 0x11, 0x11, 0x15, 0x19, 0x1e}, /*34*/
            {0x0f, 0x11, 0x11, 0x0f, 0x05, 0x09, 0x11}, /*35*/
            {0x0e, 0x11, 0x01, 0x0e, 0x10, 0x11, 0x0e}, /*36*/
            {0x1f, 0x04, 0x04, 0x04, 0x04, 0x04, 0x04}, /*37*/
            {0x00, 0x00, 0x00, 0x00, 0x00, 0x06, 0x06}, /*40*/
            {0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x0e}, /*41*/
            {0x11, 0x11, 0x11, 0x0a, 0x0a, 0x04, 0x04}, /*42*/
            {0x11, 0x11, 0x11, 0x15, 0x15, 0x15, 0x0a}, /*43*/
            {0x11, 0x11, 0x0a, 0x04, 0x0a, 0x11, 0x11}, /*44*/
            {0x11, 0x11, 0x0a, 0x04, 0x04, 0x04, 0x04}, /*45*/
            {0x1f, 0x10, 0x08, 0x04, 0x02, 0x01, 0x1f}, /*46*/
            {0x00, 0x04, 0x04, 0x1f, 0x04, 0x04, 0x00}, /*47*/
            {0x00, 0x11, 0x0a, 0x04, 0x0a, 0x11, 0x00}, /*50*/
            {0x00, 0x0a, 0x04, 0x1f, 0x04, 0x0a, 0x00}, /*51*/
            {0x1e, 0x02, 0x02, 0x02, 0x02, 0x03, 0x02}, /*52*/
            {0x00, 0x10, 0x0e, 0x0b, 0x0a, 0x0a, 0x0a}, /*53*/
            {0x00, 0x00, 0x0e, 0x11, 0x1f, 0x01, 0x0e}, /*54*/
            {0x10, 0x08, 0x04, 0x04, 0x04, 0x08, 0x10}, /*55*/
            {0x01, 0x02, 0x04, 0x04, 0x04, 0x02, 0x01}, /*56*/
            {0x00, 0x00, 0x00, 0x03, 0x03, 0x02, 0x01}, /*57*/
            {0x04, 0x0e, 0x15, 0x04, 0x04, 0x04, 0x04}, /*60*/
            {0x03, 0x13, 0x08, 0x04, 0x02, 0x19, 0x18}, /*61*/
            {0x04, 0x0c, 0x04, 0x00, 0x04, 0x06, 0x04}, /*62*/
            {0x00, 0x10, 0x08, 0x04, 0x02, 0x01, 0x00}, /*63*/
            {0x00, 0x00, 0x1f, 0x00, 0x1f, 0x00, 0x00}, /*64*/
            {0x0c, 0x0c, 0x0c, 0x00, 0x00, 0x00, 0x00}, /*65*/
            {0x11, 0x0a, 0x04, 0x0a, 0x11, 0x00, 0x00}, /*66*/
            {0x1f, 0x00, 0x11, 0x0a, 0x04, 0x0a, 0x11}, /*67*/
            {0x07, 0x08, 0x06, 0x01, 0x0f, 0x00, 0x00}, /*70*/
            {0x0e, 0x11, 0x11, 0x08, 0x04, 0x00, 0x04}, /*71*/
            {0x00, 0x04, 0x00, 0x1f, 0x00, 0x04, 0x00}, /*72*/
            {0x04, 0x0a, 0x0a, 0x0a, 0x04, 0x00, 0x04}, /*73*/
            {0x1f, 0x0a, 0x0a, 0x0a, 0x0a, 0x0a, 0x1f}, /*74*/
            {0x00, 0x00, 0x00, 0x00, 0x04, 0x0a, 0x15}, /*75*/
            {0x1f, 0x0a, 0x0a, 0x0a, 0x0a, 0x0a, 0x0a}, /*76*/
            {0x1f, 0x02, 0x04, 0x08, 0x04, 0x02, 0x1f}, /*77*/
        };

    public Printer
      (
        android.content.Context ctx
      )
      {
        final android.content.res.Resources Res = ctx.getResources();
        PaperColor = Res.getColor(R.color.paper);
        InkColor = Res.getColor(R.color.ink);
        Paper = android.graphics.Bitmap.createBitmap
          (
            /*width =*/ PaperWidth,
            /*height =*/ PaperHeight,
            /*config =*/ android.graphics.Bitmap.Config.ARGB_8888
          );
        PaperDraw = new android.graphics.Canvas(Paper);
        PaperDraw.drawPaint(GraphicsUseful.FillWithColor(PaperColor));
        Paper.prepareToDraw();
      } /*Printer*/

    public void Advance()
      /* advances the paper to the next line. */
      {
        final int[] ScrollTemp = new int[PaperWidth * (PaperHeight - LineHeight)];
        Paper.prepareToDraw();
        Paper.getPixels
          (
            /*pixels =*/ ScrollTemp,
            /*offset =*/ 0,
            /*stride =*/ PaperWidth,
            /*x =*/ 0,
            /*y =*/ LineHeight, /* lose top line */
            /*width =*/ PaperWidth,
            /*height =*/ PaperHeight - LineHeight
          );
        Paper.setPixels
          (
            /*pixels =*/ ScrollTemp,
            /*offset =*/ 0,
            /*stride =*/ PaperWidth,
            /*x =*/ 0,
            /*y =*/ 0, /* move to top */
            /*width =*/ PaperWidth,
            /*height =*/ PaperHeight - LineHeight
          );
        PaperDraw.drawRect /* fill in newly-scrolled-in area */
          (
            new android.graphics.Rect(0, PaperHeight - LineHeight, PaperWidth, PaperHeight),
            GraphicsUseful.FillWithColor(PaperColor)
          );
        Paper.prepareToDraw();
        if (ShowingView != null)
          {
            ShowingView.invalidate();
          } /*if*/
      } /*Advance*/

    public void Render
      (
        byte[] PrintReg
      )
      {
        Advance();
        final int[] Line = new int[PaperWidth * LineHeight];
        for (int i = 0; i < Line.length; ++i)
          {
          /* initialize background */
            Line[i] = PaperColor;
          } /*for*/
        for (int CharCol = 0; CharCol < Math.min(PrintReg.length, CharColumns); ++CharCol)
          {
            final int GlyphRow = (int)PrintReg[CharCol] / 10 % 10;
            final int GlyphCol = (int)PrintReg[CharCol] % 10;
            if (GlyphRow >= 0 && GlyphRow < 8 && GlyphCol >= 0 && GlyphCol < 8)
              {
                final int[] Glyph = Chars[GlyphRow * 8 + GlyphCol];
                System.err.printf("ti5x printer placing glyph %02d at col %d\n", GlyphRow * 8 + GlyphCol, CharCol); /* debug */
                for (int Row = 0; Row < CharHeight; ++Row)
                  {
                    for (int PixCol = 0; PixCol < CharWidth; ++PixCol)
                      {
                        if ((1 << PixCol & Glyph[Row]) != 0)
                          {
                          /* place a dot */
                            final int Origin =
                                    (
                                        CharCol * (CharWidth + CharHorGap)
                                    +
                                        CharHorGap
                                    +
                                        PixCol
                                    +
                                        Row * PaperWidth
                                    )
                                *
                                    (DotSize + DotGap);
                            for (int i = 0; i < DotSize; ++i)
                              {
                                for (int j = 0; j < DotSize; ++j)
                                  {
                                    Line[Origin + i * PaperWidth + j] = InkColor;
                                  } /*for*/
                              } /*for*/
                          } /*if*/
                      } /*for*/
                  } /*for*/
              } /*if*/
          } /*for*/
        Paper.setPixels
          (
            /*pixels =*/ Line,
            /*offset =*/ 0,
            /*stride =*/ PaperWidth,
            /*x =*/ 0,
            /*y =*/ PaperHeight - LineHeight,
            /*width =*/ PaperWidth,
            /*height =*/ LineHeight
          );
        if (ShowingView != null)
          {
            ShowingView.invalidate();
          } /*if*/
      } /*Render*/

    public void TextLine
      (
        String Line
      )
      {
        final byte[] Rendered = new byte[CharColumns];
        for (int i = 0; i < Math.min(Line.length(), CharColumns); ++i)
          {
            final char ch = Line.charAt(i);
            int glyph = 0;
            if (ch == ' ')
              {
                glyph = 0;
              }
            else if (ch >= 'A' && ch <= 'E')
              {
                glyph = 13 + (int)ch - (int)'A';
              }
            else if (ch >= 'F' && ch <= 'L')
              {
                glyph = 21 + (int)ch - (int)'F';
              }
            else if (ch >= 'M' && ch <= 'T')
              {
                glyph = 30 + (int)ch - (int)'M';
              }
            else if (ch >= 'U' && ch <= 'Z')
              {
                glyph = 41 + (int)ch - (int)'U';
              }
            else if (ch >= '0' && ch <= '6')
              {
                glyph = 1 + (int)ch - (int)'0';
              }
            else if (ch >= '7' && ch <= '9')
              {
                glyph = 10 + (int)ch - (int)'7';
              }
            else if (ch == '.')
              {
                glyph = 40;
              }
            else if (ch == '+')
              {
                glyph = 47;
              }
            else if (ch == '-')
              {
                glyph = 20;
              }
            else if (ch == '*')
              {
                glyph = 51;
              }
            else if (ch == '×')
              {
                glyph = 50;
              }
            else if (ch == '÷')
              {
                glyph = 72;
              }
            else if (ch == '√')
              {
                glyph = 52;
              }
            else if (ch == 'π')
              {
                glyph = 53;
              }
            else if (ch == 'e')
              {
                glyph = 54;
              }
            else if (ch == '(')
              {
                glyph = 55;
              }
            else if (ch == ')')
              {
                glyph = 56;
              }
            else if (ch == ',')
              {
                glyph = 57;
              }
            else if (ch == '↑')
              {
                glyph = 60;
              }
            else if (ch == '%')
              {
                glyph = 61;
              }
          /* 62 -- double-arrow? */
            else if (ch == '/')
              {
                glyph = 63;
              }
            else if (ch == '=')
              {
                glyph = 64;
              }
          /* 65 -- raised dot? */
          /* 66 -- superscript x? */
          /* 67 -- xbar? */
            else if (ch == '²')
              {
                glyph = 70;
              }
            else if (ch == '?')
              {
                glyph = 71;
              }
            else if (ch == '!') /*?*/
              {
                glyph = 73;
              }
            else if (ch == '♊') /* Gemini! */
              {
                glyph = 74; /* looks like pi with extra bar across bottom */
              }
          /* 75 -- bottom triangle symbol? */
            else if (ch == 'Π')
              {
                glyph = 76;
              }
            else if (ch == '∑')
              {
                glyph = 77;
              }
            else
              {
              /* leave at 0 = blank */
              } /*if*/
            Rendered[i] = (byte)glyph;
          } /*for*/
        Render(Rendered);
      } /*TextLine*/

  } /*Printer*/
