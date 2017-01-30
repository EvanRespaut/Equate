package com.llamacorp.equate;

import android.text.Spanned;

import com.llamacorp.equate.view.ViewUtils;

/**
 * Class used to store and operate on the result preview shows the user the
 * temporarily solved expression
 */
class Preview {
	private String mText;
	private Solver mSolver;
	private Expression.NumFormat mNumFormat;

	private Preview() {
		mText = "";
		mNumFormat = Expression.NumFormat.NORMAL;
	}

	Preview(Solver solver) {
		this();
		mSolver = solver;
	}


	/**
	 * Sets the preview to a solved expression.
	 *
	 * @param expr      is the expression that will be solved
	 * @param numFormat is the desired number format of the expression to be
	 *                  solved.  Set to
	 *                  {@link com.llamacorp.equate.Expression.NumFormat#ENGINEERING}
	 *                  to display an SI suffix in the result.
	 */
	public void set(Expression expr, Expression.NumFormat numFormat) {
		mNumFormat = numFormat;
		//engineering format determines if we put the preview into engineering
		//sci notation, which later gets an SI suffix
		Result res = mSolver.solve(expr, numFormat);
		if (res == null)
			setText("");
		else
			setText(res.getTextAnswer());
	}

	boolean isEmpty() {
		return mText.isEmpty();
	}

	boolean isNumFormatEngineering() {
		return mNumFormat == Expression.NumFormat.ENGINEERING;
	}

	/**
	 * Used to get text to display by the TextView
	 *
	 * @param suffixColor is the color of the SI suffix if the result in the
	 *                    preview is set to
	 *                    {@link com.llamacorp.equate.Expression.NumFormat#ENGINEERING}
	 * @return an HTML formatted result with the SI suffix in brackets if
	 * applicable
	 */
	public Spanned getText(int suffixColor) {
		String suffix = "";
		if (mNumFormat == Expression.NumFormat.ENGINEERING){
			String suffixText = SISuffixHelper.getSuffixName(mText);
			if (!suffixText.isEmpty())
				suffix = " [" + SISuffixHelper.getSuffixName(mText) + "]";
		}
		//only add on "= " if the string isn't already empty
		String returnText = mText;
		if (!"".equals(mText)) returnText = "= " + mText;

		return ViewUtils.fromHtml(returnText + "<font color=" + suffixColor
				  + ">" + suffix + "</font>");
	}


	private void setText(String text) {
		this.mText = text;
	}
}
