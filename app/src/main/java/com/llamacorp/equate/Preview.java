package com.llamacorp.equate;

import android.text.Html;
import android.text.Spanned;

/**
 * Class used to store and operate on the result preview shows the user the
 * temporarily solved expression
 */
public class Preview {
   private String mText;
   private Solver mSolver;
   private Expression.NumFormat mNumFormat;

   public Preview(){
      mText = "";
      mNumFormat = Expression.NumFormat.NORMAL;
   }

   public Preview(Solver solver) {
      this();
      mSolver = solver;
   }


   public void set(Expression expr, Expression.NumFormat numFormat) {
      mNumFormat = numFormat;
      //engineering boolean determines if we put the preview into engineering
      //sci notation, which later gets an SI suffix
      boolean engineering = false;
      if(numFormat == Expression.NumFormat.ENGINEERING) engineering = true;
      Result res = mSolver.solve(expr, engineering);
      if(res == null)
         setText("");
      else
         setText(res.getTextAnswer());
   }

   public boolean isEmpty(){
      return mText.isEmpty();
   }

   public Spanned getText(int suffixColor) {
      String suffix = "";
      if(mNumFormat == Expression.NumFormat.ENGINEERING)
         suffix = "[" + SISuffixHelper.getSuffixName(mText) + "]";

      return Html.fromHtml("= " + mText + " <font color=" + suffixColor
              + ">" + suffix + "</font>");
   }

   private void setText(String text) {
      this.mText = text;
   }
}
