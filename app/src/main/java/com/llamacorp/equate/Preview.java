package com.llamacorp.equate;

import android.text.Html;
import android.text.Spanned;

/**
 * Class used to store and operate on the result preview shows the user the
 * temporarily solved expression
 */
class Preview {
   private String mText;
   private Solver mSolver;
   private Expression.NumFormat mNumFormat;

   private Preview(){
      mText = "";
      mNumFormat = Expression.NumFormat.NORMAL;
   }

   Preview(Solver solver) {
      this();
      mSolver = solver;
   }


   public void set(Expression expr, Expression.NumFormat numFormat) {
      mNumFormat = numFormat;
      //engineering boolean determines if we put the preview into engineering
      //sci notation, which later gets an SI suffix
      boolean engineering = false;
      //if(numFormat == Expression.NumFormat.ENGINEERING) engineering = true;
      Result res = mSolver.solve(expr, numFormat);
      if(res == null)
         setText("");
      else
         setText(res.getTextAnswer());
   }

   boolean isEmpty(){
      return mText.isEmpty();
   }

   public Spanned getText(int suffixColor) {
      String suffix = "";
      if(mNumFormat == Expression.NumFormat.ENGINEERING){
         String suffixText = SISuffixHelper.getSuffixName(mText);
         if(!suffixText.isEmpty())
            suffix = "[" + SISuffixHelper.getSuffixName(mText) + "]";
      }

      return Html.fromHtml("= " + mText + " <font color=" + suffixColor
              + ">" + suffix + "</font>");
   }

   private void setText(String text) {
      this.mText = text;
   }
}
