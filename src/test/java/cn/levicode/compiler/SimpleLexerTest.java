package cn.levicode.compiler;

import org.junit.Before;
import org.junit.Test;

public class SimpleLexerTest {

    private SimpleLexer lexicalAnalyzer;

    @Before
    public void before() {
        lexicalAnalyzer = new SimpleLexer();
    }

    private void dump(TokenReader reader) {
        System.out.println("#### start #####");
        Token token = null;
        while ((token = reader.read()) != null) {
            System.out.println(token.getType() + " " + token.getText());
        }
        System.out.println("#### end #####");
    }

    @Test
    public void testJudgeStatement() {
        dump(lexicalAnalyzer.analyze("age >= 45"));
    }

    @Test
    public void testDefinition() {
        dump(lexicalAnalyzer.analyze("int iAge = 40"));
    }

    @Test
    public void testAlgorithm() {
        dump(lexicalAnalyzer.analyze("2+3*5/(4+ 2)"));
    }

    @Test
    public void testIfElse() {
        dump(lexicalAnalyzer.analyze("if(a==10) b = 1;else{b=2};"));
    }
}
