package com.llamacorp.equate;

/**
 * Class used to store and operate on the result preview shows the user the
 * temporarily solved expression
 */
public class Preview {
   private String mText;
   private Solver mSolver;

   public Preview(){
      mText = "";
   }

   public Preview(Solver solver) {
      this();
      mSolver = solver;
   }


   public void set(Expression expr) {
      Result res = mSolver.solve(expr, false);
      if(res == null)
         setText("");
      else
         setText(res.getTextAnswer());
   }

   public boolean isEmpty(){
      return getText().isEmpty();
   }

   public String getText() {
      return mText;
   }

   private void setText(String text) {
      this.mText = text;
   }

}
