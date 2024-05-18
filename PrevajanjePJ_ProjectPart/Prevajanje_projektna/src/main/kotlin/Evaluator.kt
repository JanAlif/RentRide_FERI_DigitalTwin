class Evaluator(private val tokens: List<Token>) {
    private var currentTokenIndex = 0
    private val currentToken: Token
        get() = tokens.getOrElse(currentTokenIndex) { Token(Symbol.EOF, "", -1, -1) }

    val generalPoints = mutableListOf<Point>()
    val gasPoints = mutableListOf<Point>()
    val electricPoints = mutableListOf<Point>()
    val parkingPoints = mutableListOf<Point>()
    //val startPoints = mutableListOf<Point>()

    val varPointEnv = mutableMapOf<String, Any>()

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

    fun evaluate(): CarRide {
        val result = carRide()
        if (accept(Symbol.EOF)) {
            return result
        } else {
            throw IllegalArgumentException("Invalid expression.")
        }
    }

    private fun carRide(): CarRide {
        expect(Symbol.CAR_RIDE)
        val name = strin()
        expect(Symbol.LCURLY)
        val decl = if (currentToken.symbol == Symbol.VARIABLE || currentToken.symbol == Symbol.CONST) parseDeclarations() else null
        //parseDeclarations()
        val road = road()
        val car = car()
        val start = start()
        val finish = finish()
        val cross = if (currentToken.symbol == Symbol.CROSS_SECTION) crossSections() else null
        val round = if (currentToken.symbol == Symbol.ROUNDABOUT) roundabouts() else null
        //val round = if (accept(Symbol.ROUNDABOUT)) roundabouts() else null
        val gas = if (accept(Symbol.GAS_STATION)) gas() else null
        val electricity = if (accept(Symbol.ELECTRIC_STATION)) electricity() else null
        val parking = if (accept(Symbol.PARKING)) parking() else null
        val passengers = if (currentToken.symbol == Symbol.PASSENGER) parsePassengers() else null
        //val passengers = if (accept(Symbol.PASSENGER)) parsePassengers() else null
        expect(Symbol.RCURLY)
        return CarRide(name, decl=varPointEnv , road =road,car= car,start= start,finish= finish, cross =  cross, round = round, gas=gas, electricity = electricity, parking = parking, passenger =  passengers)
    }

    private fun road(): Path {
        expect(Symbol.ROAD)
        //expect(Symbol.STRING)
        //val name = strin()
        expect(Symbol.LCURLY)
        val segments = path()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Path(segments)
    }

    private fun path(): List<PathSegment> {
        val segments = mutableListOf<PathSegment>()
        while (currentToken.symbol == Symbol.LINE || currentToken.symbol == Symbol.BEND) {
            if (currentToken.symbol == Symbol.LINE) {
                segments.add(line())
            } else if (currentToken.symbol == Symbol.BEND) {
                segments.add(bend())
            }
        }
        return segments
    }

    private fun bend(): Bend {
        accept(Symbol.BEND)
        expect(Symbol.LPAREN)
        val p1 = point()
        expect(Symbol.COMMA)
        val p2 = point()
        expect(Symbol.COMMA)
        val curve = currentToken.lexeme.toInt()
        currentTokenIndex++  // Move to the next token after reading the curve value
        expect(Symbol.RPAREN)
        expect(Symbol.SEMI_COLON)
        return Bend(p1, p2, curve)
    }

    private fun line(): Line {
        accept(Symbol.LINE)
        expect(Symbol.LPAREN)
        val p1 = point()
        expect(Symbol.COMMA)
        val p2 = point()
        expect(Symbol.RPAREN)
        expect(Symbol.SEMI_COLON)
        return Line(p1, p2)
    }
    private fun car(): Car {
        accept(Symbol.CAR)
        val str = strin()
        expect(Symbol.LCURLY)
        expect(Symbol.CAR_POINT)
        val point = point()
        expect(Symbol.SEMI_COLON)
        expect(Symbol.ID)
        expect(Symbol.COLON)
        val id = currentToken.lexeme.toInt()
        currentTokenIndex++
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Car(str, point, id)
    }

    private fun start(): Start {
        expect(Symbol.START)
        expect(Symbol.LCURLY)
        val point = point()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Start(point)
    }

    private fun finish(): Finish {
        expect(Symbol.FINISH)
        expect(Symbol.LCURLY)
        val point = point()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Finish(point)
    }

    private fun crossSections(): List<Cross> {
        val crossSections = mutableListOf<Cross>()
        while (currentToken.symbol == Symbol.CROSS_SECTION) {
            crossSections.add(cross())
        }
        return crossSections
    }

    private fun roundabouts(): List<Round> {
        val roundabouts = mutableListOf<Round>()
        while (currentToken.symbol == Symbol.ROUNDABOUT) {
            roundabouts.add(round())
        }
        return roundabouts
    }

    private fun cross(): Cross {
        accept(Symbol.CROSS_SECTION)
        val str = strin()
        expect(Symbol.LCURLY)
        val box = box()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Cross(str, box)
    }

    private fun round(): Round {
        accept(Symbol.ROUNDABOUT)
        val str = strin()
        expect(Symbol.LCURLY)
        val circ = circ()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Round(str, circ)
    }
    private fun box(): Box {
        accept(Symbol.BOX)
        expect(Symbol.LPAREN)
        val p1 = point()
        expect(Symbol.COMMA)
        val p2 = point()
        expect(Symbol.RPAREN)
        expect(Symbol.SEMI_COLON)
        return Box(p1, p2)
    }
    private fun circ(): Circ {
        accept(Symbol.CIRC)
        expect(Symbol.LPAREN)
        val point = point()
        expect(Symbol.COMMA)
        val radius = expr()
        expect(Symbol.RPAREN)
        expect(Symbol.SEMI_COLON)
        return Circ(point, radius)
    }

    private fun parsePassengers(): List<Passenger> {
        val passengers = mutableListOf<Passenger>()
        while (currentToken.symbol == Symbol.PASSENGER) {
            passengers.add(passenger())
        }
        return passengers
    }

    private fun passenger(): Passenger {
        accept(Symbol.PASSENGER)
        val str = strin()
        expect(Symbol.LCURLY)
        val start = start()
        val finish = finish()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Passenger(str, start, finish)
    }

    private fun parseDeclarations() {
        while (currentToken.symbol == Symbol.VARIABLE || currentToken.symbol == Symbol.CONST) {
            val declaration = declaration()
            if (declaration != null) {
                declaration.eval(varPointEnv)
            }
        }
    }

    private fun declaration(): VarPointDeclarations {
        return if (currentToken.symbol == Symbol.VARIABLE) {
            val variable = variable()
            expect(Symbol.ASSIGN)
            val expr = expr()
            expect(Symbol.SEMI_COLON)
            varPointEnv[variable.name] = expr
            VarDeclaration(variable, expr)
        } else if (currentToken.symbol == Symbol.CONST) {
            expect(Symbol.CONST)
            val variable = variable()
            expect(Symbol.ASSIGN)
            val point = point()
            expect(Symbol.SEMI_COLON)
            varPointEnv[variable.name] = point
            PointDeclaration(variable, point)
        } else {
            throw IllegalArgumentException("Invalid declaration")
        }
    }

    private fun varDecl(): Boolean {
        return currentToken.symbol == Symbol.VARIABLE
    }

    private fun pointDecl(): Boolean {
        return currentToken.symbol == Symbol.CONST
    }

    private fun electricity(): Electricity {
        accept(Symbol.ELECTRIC_STATION)
        val str = strin()
        expect(Symbol.LCURLY)
        val points = points(pointType = "electric")
        val filter = filter()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Electricity(str, points, filter)
    }

    private fun gas(): Gas {
        accept(Symbol.GAS_STATION)
        val str = strin()
        expect(Symbol.LCURLY)
        val points = points(pointType = "gas")
        val filter = filter()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Gas(str, points, filter)
    }

    private fun parking(): Parking {
        accept(Symbol.PARKING)
        val str = strin()
        expect(Symbol.LCURLY)
        val points = points(pointType = "parking")
        val filter = filter()
        expect(Symbol.RCURLY)
        expect(Symbol.SEMI_COLON)
        return Parking(str, points, filter)
    }

    private fun filter(): Filter {
        expect(Symbol.LET)
        val variable = variable()
        expect(Symbol.ASSIGN)
        expect(Symbol.NEIGH)
        expect(Symbol.LPAREN)
        val point = point()
        expect(Symbol.COMMA)
        val expr = expr()
        expect(Symbol.RPAREN)
        expect(Symbol.SEMI_COLON)
        val foreach = foreach(variable)
        return Filter(variable, point, expr, foreach)

    }

    private fun foreach(declaredVariable: Variable): Foreach {
        expect(Symbol.FOREACH)
        val variable1 = variable()  // Parse iterating variable
        expect(Symbol.IN)
        val variable2 = variable()  // Parse source collection variable
        expect(Symbol.LCURLY)
        expect(Symbol.HIGHLIGHT)
        val variable3 = variable()
        if (variable3 != variable1) {
            throw IllegalArgumentException("Highlighted variable ${variable3.name} does not match iterating variable ${variable1.name}")
        }
        if (variable2.name != declaredVariable.name) {
            throw IllegalArgumentException("Source variable ${variable2.name} does not match declared variable ${declaredVariable.name}")
        }
        expect(Symbol.RCURLY)
        return Foreach(variable1, variable2)
    }

    private fun points(pointType: String = ""): Points {
        val pointList = mutableListOf<Point>()
        while (true) {
            if (currentToken.symbol == Symbol.LPAREN || currentToken.symbol == Symbol.VARIABLE) {
                val point = point(pointType)
                pointList.add(point)
                if (!accept(Symbol.SEMI_COLON) && currentToken.symbol != Symbol.EOF) {
                    throw IllegalArgumentException("Expected semicolon after a point")
                }
            } else {
                break
            }
        }
        return Points(pointList)
    }

    private fun point(pointType: String = ""): Point {
        if (currentToken.symbol == Symbol.VARIABLE) {
            // Handle variable case
            val variable = variable()
            val point = varPointEnv[variable.name] as? Point
                ?: throw IllegalArgumentException("Variable ${variable.name} is not a point or not defined")
            when (pointType) {
                "gas" -> gasPoints.add(point)
                "electric" -> electricPoints.add(point)
                "parking" -> parkingPoints.add(point)
                else -> generalPoints.add(point)
            }
            return point
        } else {
            // Handle regular point parsing
            expect(Symbol.LPAREN)
            val p1 = expr()
            expect(Symbol.COMMA)
            val p2 = expr()
            expect(Symbol.RPAREN)
            val point = Point(p1, p2, type = pointType)
            when (pointType) {
                "gas" -> gasPoints.add(point)
                "electric" -> electricPoints.add(point)
                "parking" -> parkingPoints.add(point)
                else -> generalPoints.add(point)
            }
            return point
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

    private fun strin(): Strin {
        //expect(Symbol.STRING) // TODO change her was made
        val content = currentToken.lexeme  // Assuming the current token contains the string content
        currentTokenIndex++
        return Strin(content)
    }

    private fun variable(): Variable {
        // Ensure the current token is a variable identifier before proceeding.
        if (currentToken.symbol != Symbol.VARIABLE) {
            throw IllegalArgumentException("Expected variable identifier, found ${currentToken.symbol}")
        }

        // Retrieve the name of the variable from the current token's lexeme.
        val variableName = currentToken.lexeme
        currentTokenIndex++  // Move past the variable token.

        // Return a new Variable object with the captured name.
        return Variable(variableName)
    }
}
