package cn.levicode.compiler;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleLexer {

    private enum DfaStatus {
        Initial,
        Id,
        IntLiteral,
        Plus,   // +
        Minus,  // -
        Star,   // *
        Slash,  // /
        GT,     // >
        GE,     // >=
        EQ,     // ==
        LE,     // <=
        LT,     // <
        Assignment,// =
        SemiColon, // ;
        LeftParen, // (
        RightParen,// )
        Id_int1,
        Id_int2,
        Id_int3,
        Id_if1,
        Id_if2,
        Id_else1,
        Id_else2,
        Id_else3,
        Id_else4,
        If,
        Else,
    }

    private StringBuilder tokenText = new StringBuilder();
    private SimpleToken token = new SimpleToken();
    private List<Token> tokenList = new ArrayList<Token>();

    public TokenReader analyze(String text) {
        if (text == null) {
            return new SimpleTokenReader(Collections.<Token>emptyList());
        }

        CharArrayReader reader = new CharArrayReader(text.toCharArray());
        DfaStatus state = DfaStatus.Initial;
        int intChar = 0;

        try {
            while ((intChar = reader.read()) != -1) {
                char ch = (char) intChar;

                switch (state) {
                    case Initial:
                        state = initToken(ch);
                        break;
                    case GE:
                    case LE:
                    case EQ:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                    case LeftParen:
                    case RightParen:
                        state = initToken(ch);
                        break;
                    case GT:
                        if (ch == '=') {
                            state = DfaStatus.GE;
                            token.type = TokenType.GE;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case LT:
                        if (ch == '=') {
                            state = DfaStatus.LE;
                            token.type = TokenType.LE;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                    case Assignment:
                        if (ch == '=') {
                            state = DfaStatus.EQ;
                            token.type = TokenType.EQ;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id:
                        if (isAlpha(ch) || isDigit(ch)) {
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {
                            state = DfaStatus.Id_int2;
                            tokenText.append(ch);
                        } else if (ch == 'f') {
                            state = DfaStatus.Id_if2;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = DfaStatus.Id_int3;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBlank(ch)) {
                            token.type = TokenType.Int;
                            state = initToken(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_if2:
                        if (isBlank(ch) || ch == '(') {
                            token.type = TokenType.If;
                            state = initToken(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            initToken(ch);
                        }
                        break;
                    case Id_else1:
                        if (ch == 'l') {
                            state = DfaStatus.Id_else2;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            initToken(ch);
                        }
                        break;
                    case Id_else2:
                        if (ch == 's') {
                            state = DfaStatus.Id_else3;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            initToken(ch);
                        }
                        break;
                    case Id_else3:
                        if (ch == 'e') {
                            state = DfaStatus.Id_else4;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            initToken(ch);
                        }
                        break;
                    case Id_else4:
                        if (isBlank(ch) || ch == '{') {
                            token.type = TokenType.Else;
                            state = initToken(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaStatus.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    default:
                        break;
                }
            }
            // put last token
            if (tokenText.length() > 0) {
                token.text = tokenText.toString();
                tokenList.add(token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SimpleTokenReader(tokenList);
    }

    private boolean isBlank(char ch) {
        return ch >= ' ';
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private DfaStatus initToken(char ch) {
        // put old token
        if (tokenText.length() > 0) {
            token.text = tokenText.toString();
            tokenList.add(token);

            token = new SimpleToken();
            tokenText = new StringBuilder();
        }

        DfaStatus newState = DfaStatus.Initial;
        if (isAlpha(ch)) {
            if (ch == 'i') {
                newState = DfaStatus.Id_int1;
            } else if (ch == 'e') {
                newState = DfaStatus.Id_else1;
            } else {
                newState = DfaStatus.Id;
            }
            token.type = TokenType.Identifier;
            tokenText.append(ch);
        } else if (isDigit(ch)) {
            newState = DfaStatus.IntLiteral;
            token.type = TokenType.IntLiteral;
            tokenText.append(ch);
        } else if (ch == '>') {
            newState = DfaStatus.GT;
            token.type = TokenType.GT;
            tokenText.append(ch);
        } else if (ch == '<') {
            newState = DfaStatus.LT;
            token.type = TokenType.LT;
            tokenText.append(ch);
        } else if (ch == '+') {
            newState = DfaStatus.Plus;
            token.type = TokenType.Plus;
            tokenText.append(ch);
        } else if (ch == '-') {
            newState = DfaStatus.Minus;
            token.type = TokenType.Minus;
            tokenText.append(ch);
        } else if (ch == '*') {
            newState = DfaStatus.Star;
            token.type = TokenType.Star;
            tokenText.append(ch);
        } else if (ch == '/') {
            newState = DfaStatus.Slash;
            token.type = TokenType.Slash;
            tokenText.append(ch);
        } else if (ch == '=') {
            newState = DfaStatus.Assignment;
            token.type = TokenType.Assignment;
            tokenText.append(ch);
        } else if (ch == ';') {
            newState = DfaStatus.SemiColon;
            token.type = TokenType.SemiColon;
            tokenText.append(ch);
        } else if (ch == '(') {
            newState = DfaStatus.LeftParen;
            token.type = TokenType.LeftParen;
            tokenText.append(ch);
        } else if (ch == ')') {
            newState = DfaStatus.RightParen;
            token.type = TokenType.RightParen;
            tokenText.append(ch);
        }

        return newState;
    }

    private static class SimpleToken implements Token {

        private TokenType type;

        private String text;

        public TokenType getType() {
            return type;
        }

        public String getText() {
            return text;
        }
    }

    private static class SimpleTokenReader implements TokenReader {

        List<Token> tokens;

        int pos = 0;

        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
        }

        public Token read() {
            if (pos < tokens.size()) {
                return tokens.get(pos++);
            }
            return null;
        }

        public Token peek() {
            if (pos < tokens.size()) {
                return tokens.get(pos);
            }
            return null;
        }

        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        public int getPosition() {
            return pos;
        }

        public void setPosition(int position) {
            if (position >= 0 && position < tokens.size()) {
                this.pos = position;
            }
        }
    }
}
