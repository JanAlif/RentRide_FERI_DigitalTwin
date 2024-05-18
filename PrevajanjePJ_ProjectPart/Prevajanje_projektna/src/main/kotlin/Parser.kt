import kotlin.coroutines.AbstractCoroutineContextElement

class Parser(private val tokens: List<Token>) {
    private var currentTokenIndex = 0
    // Getter for the current token. If the current index is out of bounds, returns an EOF (End Of File) token.
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

    fun parse(): Boolean {
        return try {
            val isValid = carRide() && accept(Symbol.EOF)
            println(if (isValid) "accept" else "reject")
            isValid
        } catch (e: Error) {
            println("Reject: ${e.message}")
            false
        }
    }

    private fun carRide(): Boolean{
        return accept(Symbol.CAR_RIDE) &&
                accept(Symbol.STRING) &&
                accept(Symbol.LCURLY) &&
                declarations() &&
                road() &&
                car() &&
                start() &&
                finish() &&
                cross() &&
                round() &&
                gas() &&
                electricity() &&
                parking() &&
                passengers() &&
                accept(Symbol.RCURLY)
    }

    private fun declarations(): Boolean{
        if (declaration()) {
            // If successful, recursively attempt to parse more declarations.
            return declarations()
        }
        // If no declaration could be parsed, handle the epsilon transition (an empty sequence),
        // which is valid and indicates the end of declarations.
        return true
    }

    private fun declaration(): Boolean{
        if (varDecl()) {
            return true
        }
        // If parsing a variable declaration fails, try to parse a point declaration.
        if (pointDecl()) {
            return true
        }
        // If neither can be parsed, return false to signal failure to parse any declaration.
        return false
    }

    private fun varDecl(): Boolean{
        return accept(Symbol.VARIABLE) &&
                accept(Symbol.ASSIGN) &&
                expr() &&
                accept(Symbol.SEMI_COLON)
    }

    private fun pointDecl(): Boolean{
        return accept(Symbol.CONST) &&
                accept(Symbol.VARIABLE) &&
                accept(Symbol.ASSIGN) &&
                point() &&
                accept(Symbol.SEMI_COLON)
    }

    private fun road(): Boolean{
        return accept(Symbol.ROAD) &&
                accept(Symbol.LCURLY) &&
                path() &&
                accept(Symbol.RCURLY)&&
                accept(Symbol.SEMI_COLON)
    }

    private fun path(): Boolean{
        if (line()) {
            return path()
        }

        // If LINE was not found, attempt to parse a BEND, followed by recursive PATH
        if (bend()) {
            return path()
        }

        // If neither LINE nor BEND were found, handle the epsilon transition (empty sequence)
        return true
    }

    private fun line(): Boolean{
        return accept(Symbol.LINE) &&
                accept(Symbol.LPAREN) &&
                point() &&
                accept(Symbol.COMMA) &&
                point() &&
                accept(Symbol.RPAREN) &&
                accept(Symbol.SEMI_COLON)
    }

    private fun bend(): Boolean{
        return accept(Symbol.BEND) &&
                accept(Symbol.LPAREN) &&
                point() &&
                accept(Symbol.COMMA) &&
                point() &&
                accept(Symbol.COMMA) &&
                accept(Symbol.INT) &&
                accept(Symbol.RPAREN) &&
                accept(Symbol.SEMI_COLON)
    }

    private fun car(): Boolean{
        return accept(Symbol.CAR) &&
                accept(Symbol.STRING) &&
                accept(Symbol.LCURLY) &&
                accept(Symbol.CAR_POINT) &&
                point() &&
                accept(Symbol.SEMI_COLON) &&
                accept(Symbol.ID) &&
                accept(Symbol.COLON) &&
                accept(Symbol.INT) &&
                accept(Symbol.RCURLY) &&
                accept(Symbol.SEMI_COLON)
    }

    private fun start(): Boolean{
        return accept(Symbol.START) &&
                accept(Symbol.LCURLY) &&
                point() &&
                accept(Symbol.RCURLY)&&
                accept(Symbol.SEMI_COLON)
    }
    private fun finish(): Boolean{
        return accept(Symbol.FINISH) &&
                accept(Symbol.LCURLY) &&
                point() &&
                accept(Symbol.RCURLY)&&
                accept(Symbol.SEMI_COLON)
    }

    private fun cross(): Boolean{
        if (!accept(Symbol.CROSS_SECTION)) {
            return true  // Handle the epsilon case (no "parking" keyword means no parking section)
        }
        if (!accept(Symbol.STRING)) {
            return false  // Missing the required string
        }
        if (!accept(Symbol.LCURLY)) {
            return false  // Missing the opening brace
        }
        if (!box()) {
            return false  // POINTS parsing failed
        }
        if (!accept(Symbol.RCURLY)) {
            return false  // Missing the closing brace
        }
        if (!accept(Symbol.SEMI_COLON)) {
            return false  // Missing the required semicolon
        }
        if (!cross()) {
            return false  // Missing the required semicolon
        }
        return true  // Successfully parsed the whole structure
    }

    private fun round(): Boolean{
        if (!accept(Symbol.ROUNDABOUT)) {
            return true  // Handle the epsilon case (no "parking" keyword means no parking section)
        }
        if (!accept(Symbol.STRING)) {
            return false  // Missing the required string
        }
        if (!accept(Symbol.LCURLY)) {
            return false  // Missing the opening brace
        }
        if (!circ()) {
            return false  // POINTS parsing failed
        }
        if (!accept(Symbol.RCURLY)) {
            return false  // Missing the closing brace
        }
        if (!accept(Symbol.SEMI_COLON)) {
            return false  // Missing the required semicolon
        }
        if (!round()) {
            return false  // Missing the required semicolon
        }
        return true  // Successfully parsed the whole structure
    }

    private fun box(): Boolean{
        return accept(Symbol.BOX) &&
                accept(Symbol.LPAREN) &&
                point() &&
                accept(Symbol.COMMA)&&
                point() &&
                accept(Symbol.RPAREN)&&
                accept(Symbol.SEMI_COLON)
    }

    private fun circ(): Boolean{
        return accept(Symbol.CIRC) &&
                accept(Symbol.LPAREN) &&
                point() &&
                accept(Symbol.COMMA)&&
                expr() &&
                accept(Symbol.RPAREN)&&
                accept(Symbol.SEMI_COLON)
    }

    private fun gas(): Boolean{
        if (!accept(Symbol.GAS_STATION)) {
            return true  // Handle the epsilon case (no "parking" keyword means no parking section)
        }
        if (!accept(Symbol.STRING)) {
            return false  // Missing the required string
        }
        if (!accept(Symbol.LCURLY)) {
            return false  // Missing the opening brace
        }
        if (!points()) {
            return false  // POINTS parsing failed
        }
        if (!filter()) {
            return false  // FILTER parsing failed
        }
        if (!accept(Symbol.RCURLY)) {
            return false  // Missing the closing brace
        }
        if (!accept(Symbol.SEMI_COLON)) {
            return false  // Missing the required semicolon
        }
        return true  // Successfully parsed the whole structure
    }

    private fun electricity(): Boolean{
        if (!accept(Symbol.ELECTRIC_STATION)) {
            return true  // Handle the epsilon case (no "parking" keyword means no parking section)
        }
        if (!accept(Symbol.STRING)) {
            return false  // Missing the required string
        }
        if (!accept(Symbol.LCURLY)) {
            return false  // Missing the opening brace
        }
        if (!points()) {
            return false  // POINTS parsing failed
        }
        if (!filter()) {
            return false  // FILTER parsing failed
        }
        if (!accept(Symbol.RCURLY)) {
            return false  // Missing the closing brace
        }
        if (!accept(Symbol.SEMI_COLON)) {
            return false  // Missing the required semicolon
        }
        return true  // Successfully parsed the whole structure
    }

    private fun parking(): Boolean{
        if (!accept(Symbol.PARKING)) {
            return true  // Handle the epsilon case (no "parking" keyword means no parking section)
        }
        if (!accept(Symbol.STRING)) {
            return false  // Missing the required string
        }
        if (!accept(Symbol.LCURLY)) {
            return false  // Missing the opening brace
        }
        if (!points()) {
            return false  // POINTS parsing failed
        }
        if (!filter()) {
            return false  // FILTER parsing failed
        }
        if (!accept(Symbol.RCURLY)) {
            return false  // Missing the closing brace
        }
        if (!accept(Symbol.SEMI_COLON)) {
            return false  // Missing the required semicolon
        }
        return true  // Successfully parsed the whole structure
    }

    private fun filter(): Boolean{
        return accept(Symbol.LET) &&
                accept(Symbol.VARIABLE) &&
                accept(Symbol.ASSIGN) &&
                accept(Symbol.NEIGH) &&
                accept(Symbol.LPAREN) &&
                point() &&
                accept(Symbol.COMMA) &&
                expr() &&
                accept(Symbol.RPAREN) &&
                accept(Symbol.SEMI_COLON) &&
                foreach()
    }

    private fun foreach(): Boolean{
        return accept(Symbol.FOREACH) &&
                accept(Symbol.VARIABLE) &&
                accept(Symbol.IN) &&
                accept(Symbol.VARIABLE) &&
                accept(Symbol.LCURLY) &&
                accept(Symbol.HIGHLIGHT) &&
                accept(Symbol.VARIABLE) &&
                accept(Symbol.RCURLY)
    }

    private fun passengers(): Boolean{
        while (passenger()) {
            // The passenger function handles one PASSENGER entry.
            // Loop continues as long as passenger entries are correctly parsed.
        }
        return true // Always returns true because the epsilon transition (empty sequence) is always valid.
    }

    private fun passenger(): Boolean{
        return accept(Symbol.PASSENGER) &&
                accept(Symbol.STRING) &&
                accept(Symbol.LCURLY) &&
                start() && finish() &&
                accept(Symbol.RCURLY) &&
                accept(Symbol.SEMI_COLON)
    }

    private fun points(): Boolean{
        if (!point()) {
            return true
        }
        // If a point is parsed, a semicolon must follow. If not, it's an error.
        if (!accept(Symbol.SEMI_COLON)) {
            return false
        }
        // Recursively call parsePoints to handle the continuation of POINT ";" POINTS
        return points()
    }

    private fun point(): Boolean{
        return accept(Symbol.LPAREN) &&
                expr() &&
                accept(Symbol.COMMA) &&
                expr() &&
                expect(Symbol.RPAREN)
    }

    private fun expr(): Boolean {
        return additive()
    }

    private fun additive(): Boolean {
        if (!multiplicative()) return false
        return additive_()
    }

    private fun additive_(): Boolean {
        return when {
            accept(Symbol.PlUS) -> multiplicative() && additive_()
            accept(Symbol.MINUS) -> multiplicative() && additive_()
            else -> true // Epsilon transition represents the end of this recursive method.
        }
    }

    private fun multiplicative(): Boolean {
        if (!unary()) return false
        return multiplicative_()
    }

    private fun multiplicative_(): Boolean {
        return when {
            accept(Symbol.TIMES) -> unary() && multiplicative_()
            accept(Symbol.DIVIDES) -> unary() && multiplicative_()
            else -> true // Epsilon transition.
        }
    }

    private fun unary(): Boolean {
        return when {
            accept(Symbol.PlUS) -> primary()
            accept(Symbol.MINUS) -> primary()
            else -> primary()
        }
    }

    private fun primary(): Boolean {
        return when {
            accept(Symbol.INT) -> true
            accept(Symbol.REAL) -> true
            accept(Symbol.VARIABLE) -> true
            accept(Symbol.LPAREN) -> {
                val valid = additive()
                expect(Symbol.RPAREN)
                valid
            }
            else -> false
        }
    }

}

// Method returns a list of tokens
fun collectTokens(scanner: Scanner): List<Token> {
    val tokens = mutableListOf<Token>()

    var token = scanner.getToken()
    while (token.symbol != Symbol.EOF) {
        tokens.add(token)
        token = scanner.getToken()
    }

    return tokens
}