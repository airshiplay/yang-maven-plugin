// Generated from Demo.g4 by ANTLR 4.3
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DemoParser}.
 */
public interface DemoListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DemoParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(@NotNull DemoParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemoParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(@NotNull DemoParser.StatContext ctx);

	/**
	 * Enter a parse tree produced by {@link DemoParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(@NotNull DemoParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemoParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(@NotNull DemoParser.ExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link DemoParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(@NotNull DemoParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemoParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(@NotNull DemoParser.AtomContext ctx);

	/**
	 * Enter a parse tree produced by {@link DemoParser#multExpr}.
	 * @param ctx the parse tree
	 */
	void enterMultExpr(@NotNull DemoParser.MultExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemoParser#multExpr}.
	 * @param ctx the parse tree
	 */
	void exitMultExpr(@NotNull DemoParser.MultExprContext ctx);

	/**
	 * Enter a parse tree produced by {@link DemoParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(@NotNull DemoParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemoParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(@NotNull DemoParser.ProgContext ctx);
}