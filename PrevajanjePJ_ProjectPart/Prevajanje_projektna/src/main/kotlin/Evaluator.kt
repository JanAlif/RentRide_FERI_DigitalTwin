class Evaluator(private val tokens: List<Token>) {
    private var currentTokenIndex = 0
    private val currentToken: Token
        get() = tokens.getOrElse(currentTokenIndex) { Token(Symbol.EOF, "", -1, -1) }

    // Accepts the current token if it matches the given symbol and advances to the next token.
    private fun accept(symbol: Symbol): Boolean {
        if (currentToken.symbol == symbol) {
            currentTokenIndex++
            return true
        }
        return false
    }

    // Expects the current token to match the given symbol and advances to the next token; throws an error otherwise.
    private fun expect(symbol: Symbol): Boolean {
        if (accept(symbol)) return true
        throw Error("Expected $symbol, found ${currentToken.symbol}")
    }

    fun evaluate(): Expr  {
        val result = expr()
        if (accept(Symbol.EOF)) {
            return result
        } else {
            throw IllegalArgumentException("Invalid expression.")
        }
    }

    private fun expr(): Expr {
        return additive()
    }

    private fun additive(): Expr {
        var expr = parseMultiplicative()
        while (currentToken.symbol == Symbol.PlUS || currentToken.symbol == Symbol.MINUS) {
            val op = currentToken.symbol
            currentTokenIndex++  // Advance to next token
            val right = parseMultiplicative()
            expr = if (op == Symbol.PlUS) Plus(expr, right) else Minus(expr, right)
        }
        return expr
    }

    private fun parseMultiplicative(): Expr {
        var expr = parseUnary()
        while (currentToken.symbol == Symbol.TIMES || currentToken.symbol == Symbol.DIVIDES) {
            val op = currentToken.symbol
            currentTokenIndex++  // Advance to next token
            val right = parseUnary()
            expr = if (op == Symbol.TIMES) Times(expr, right) else Divides(expr, right)
        }
        return expr
    }

    private fun parseUnary(): Expr {
        if (accept(Symbol.PlUS)) {
            val expr = parsePrimary()
            return UnaryPlus(expr)
        } else if (accept(Symbol.MINUS)) {
            val expr = parsePrimary()
            return UnaryMinus(expr)
        } else {
            return parsePrimary()
        }
    }

    private fun parsePrimary(): Expr {
        when (currentToken.symbol) {
            Symbol.INT, Symbol.REAL -> {
                val value = currentToken.lexeme.toDouble()
                currentTokenIndex++  // Advance to next token
                return Real(value)
            }
            Symbol.VARIABLE -> {
                val name = currentToken.lexeme
                currentTokenIndex++  // Advance to next token
                return Variable(name)
            }
            Symbol.LPAREN -> {
                currentTokenIndex++  // Consume '('
                val expr = additive()
                expect(Symbol.RPAREN)  // Expect and consume ')'
                return expr
            }
            else -> throw IllegalArgumentException("Unexpected token ${currentToken.symbol} at parsePrimary")
        }
    }
}
