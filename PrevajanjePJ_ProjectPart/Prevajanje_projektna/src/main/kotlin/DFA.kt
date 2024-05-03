import java.io.InputStream
import java.io.OutputStream


enum class Symbol {
    EOF,
    SKIP,
    PlUS,
    MINUS,
    TIMES,
    DIVIDES,
    LPAREN,
    RPAREN,
    LCURLY,
    RCURLY,
    VARIABLE,
    REAL,
    ASSIGN,
    CONST,
    START,
    FINISH,
    STRING,
    PASSENGER,
    PARKING,
    ELECTRIC_STATION,
    GAS_STATION,
    FOREACH,
    IN,
    HIGHLIGHT,
    LET,
    NEIGH,
    SEMI_COLON,
    CROSS_SECTION,
    BOX,
    ROUNDABOUT,
    CIRC,
    CAR,
    CAR_POINT,
    ID,
    COLON,
    BEND,
    LINE,
    ROAD,
    CAR_RIDE,
    COMMA
}

const val ERROR_STATE = 0
const val EOF = -1
const val NEWLINE = '\n'.code

interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Symbol
    val startState: Int
    val finalStates: Set<Int>
}

object RentRideDFA: DFA {
    override val states = (1 .. 250).toSet()
    override val alphabet = 0 .. 255
    override val startState = 1
    override val finalStates = setOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 24, 29, 34, 43, 50, 65, 75, 82, 91, 94, 99, 111, 121, 125, 128, 136, 140, 144, 148, 155, 158, 160, 161, 163, 165, 166)

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 1 // plus the EOF
    private val transitions = Array(numberOfStates) {IntArray(numberOfCodes)}
    private val values = Array(numberOfStates) {Symbol.SKIP}

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    private fun setSymbol(state: Int, symbol: Symbol) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Symbol {
        assert(states.contains(state))
        return values[state]
    }
    init {
        //START ALL MISC SYMBOLS
        setTransition(1, EOF, 2)
        setTransition(1, ' ', 3)
        setTransition(1, '\t', 4)
        setTransition(1, '\n', 5)
        setSymbol(2, Symbol.EOF)

        //BASIC OPERATIONS
        setTransition(1, '*', 6)
        setSymbol(6, Symbol.TIMES)
        setTransition(1, '+', 7)
        setSymbol(7, Symbol.PlUS)
        setTransition(1, '/', 8)
        setSymbol(8, Symbol.DIVIDES)
        setTransition(1, '-', 9)
        setSymbol(9, Symbol.MINUS)

        //ALL BRACES
        setTransition(1, '(', 10)
        setSymbol(10, Symbol.LPAREN)
        setTransition(1, ')', 11)
        setSymbol(11, Symbol.RPAREN)
        setTransition(1, '{', 12)
        setSymbol(12, Symbol.LCURLY)
        setTransition(1, '}', 13)
        setSymbol(13, Symbol.RCURLY)

        //OTHER 1 CHARACTER TERMINALS
        setTransition(1, '=', 14)
        setSymbol(14, Symbol.ASSIGN)
        setTransition(1, ';', 15)
        setSymbol(15, Symbol.SEMI_COLON)
        setTransition(1, ':', 16)
        setSymbol(16, Symbol.COLON)

        //2 CHARACTER TERMINALS
        setTransition(1, 'i', 17)
        setTransition(17, 'n', 18)
        setSymbol(18, Symbol.IN)
        setTransition(17, 'd', 19)
        setSymbol(19, Symbol.ID)

        //RESERVED WORDS OR CONSTANT WORDS
        //CONST, START, FINISH, PASSENGER, PARKING, ELECTRIC STATION, GAS STATION FOREACH, HIGHLIGHT, LET NEIGH, CROSS SECTION, ROUNDABOUT, CIRC, CAR, CAR POINT, BEND, LINE , ROAD, CAR RIDE
        setTransition(1, 'c', 20)
        setTransition(20, 'o', 21)
        setTransition(21, 'n', 22)
        setTransition(22, 's', 23)
        setTransition(23, 't', 24)
        /*
        setSymbol(20, Symbol.VARIABLE)
        setSymbol(21, Symbol.VARIABLE)
        setSymbol(22, Symbol.VARIABLE)
        setSymbol(23, Symbol.VARIABLE)

        for (letter in 'a'..'z') {
            if (letter != 'o'){
                setTransition(20, letter.code, 164)
            }
            if (letter != 'n'){
                setTransition(21, letter.code, 164)
            }
            if (letter != 's'){
                setTransition(22, letter.code, 164)
            }
            if (letter != 't'){
                setTransition(23, letter.code, 164)
            }
        }

         */
        setSymbol(24, Symbol.CONST)
        setTransition( 1,'s',25 )
        setTransition(25, 't', 26)
        setTransition(26, 'a', 27)
        setTransition(27, 'r', 28)
        setTransition(28, 't', 29)
        setSymbol(29, Symbol.START)
        setTransition( 1,'f',29 )
        setTransition(29, 'i', 30)
        setTransition(30, 'n', 31)
        setTransition(31, 'i', 32)
        setTransition(32, 's', 33)
        setTransition(33, 'h', 34)
        setSymbol(34, Symbol.FINISH)
        setTransition( 1,'p',35)
        setTransition(35, 'a', 36)
        setTransition(36, 's', 37)
        setTransition(37, 's', 38)
        setTransition(38, 'e', 39)
        setTransition(39, 'n', 40)
        setTransition(40, 'g', 41)
        setTransition(41, 'e', 42)
        setTransition(42, 'r', 43)
        setSymbol(43, Symbol.PASSENGER)
        setTransition(36, 'r', 46)
        setTransition(46, 'k', 47)
        setTransition(47, 'i', 48)
        setTransition(48, 'n', 49)
        setTransition(49, 'g', 50)
        setSymbol(50, Symbol.PARKING)
        setTransition(1, 'e', 51)
        setTransition(51, 'l', 52)
        setTransition(52, 'e', 53)
        setTransition(53, 'c', 54)
        setTransition(54, 't', 55)
        setTransition(55, 'r', 56)
        setTransition(56, 'i', 57)
        setTransition(57, 'c', 58)
        setTransition(58, 'S', 59)
        setTransition(59, 't', 60)
        setTransition(60, 'a', 61)
        setTransition(61, 't', 62)
        setTransition(62, 'i', 63)
        setTransition(63, 'o', 64)
        setTransition(64, 'n', 65)
        setSymbol(65, Symbol.ELECTRIC_STATION)
        setTransition(1, 'g', 66)
        setTransition(66, 'a', 67)
        setTransition(67, 's', 68)
        setTransition(68, 'S', 69)
        setTransition(69, 't', 70)
        setTransition(70, 'a', 71)
        setTransition(71, 't', 72)
        setTransition(72, 'i', 73)
        setTransition(73, 'o', 74)
        setTransition(74, 'n', 75)
        setSymbol(75, Symbol.GAS_STATION)
        setTransition(29, 'o', 77)
        setTransition(77, 'r', 78)
        setTransition(78, 'e', 79)
        setTransition(79, 'a', 80)
        setTransition(80, 'c', 81)
        setTransition(81, 'h', 82)
        setSymbol(82, Symbol.FOREACH)
        setTransition(1, 'h', 83)
        setTransition(83, 'i', 84)
        setTransition(84, 'g', 85)
        setTransition(85, 'h', 86)
        setTransition(86, 'l', 87)
        setTransition(87, 'i', 88)
        setTransition(88, 'g', 89)
        setTransition(89, 'h', 90)
        setTransition(90, 't', 91)
        setSymbol(91, Symbol.HIGHLIGHT)
        setTransition(1, 'l', 92)
        setTransition(92, 'e', 93)
        setTransition(93, 't', 94)
        setSymbol(94, Symbol.LET)
        setTransition(1, 'n', 95)
        setTransition(95, 'e', 96)
        setTransition(96, 'i', 97)
        setTransition(97, 'g', 98)
        setTransition(98, 'h', 99)
        setSymbol(99, Symbol.NEIGH)
        setTransition(20, 'r', 101)
        setTransition(101, 'o', 102)
        setTransition(102, 's', 103)
        setTransition(103, 's', 104)
        setTransition(104, 'S', 105)
        setTransition(105, 'e', 106)
        setTransition(106, 'c', 107)
        setTransition(107, 't', 108)
        setTransition(108, 'i', 109)
        setTransition(109, 'o', 110)
        setTransition(110, 'n', 111)
        setSymbol(111, Symbol.CROSS_SECTION)
        setTransition(1, 'r', 112)
        setTransition(112, 'o', 113)
        setTransition(113, 'u', 114)
        setTransition(114, 'n', 115)
        setTransition(115, 'd', 116)
        setTransition(116, 'a', 117)
        setTransition(117, 'b', 118)
        setTransition(118, 'o', 119)
        setTransition(119, 'u', 120)
        setTransition(120, 't', 121)
        setSymbol(121, Symbol.ROUNDABOUT)
        setTransition(20, 'i', 123)
        setTransition(123, 'r', 124)
        setTransition(124, 'c', 125)
        setSymbol(125, Symbol.CIRC)
        setTransition(20, 'a', 127)
        setTransition(127, 'r', 128)
        setSymbol(128, Symbol.CAR)
        setTransition(128, 'P', 132)
        setTransition(132, 'o', 133)
        setTransition(133, 'i', 134)
        setTransition(134, 'n', 135)
        setTransition(135, 't', 136)
        setSymbol(136, Symbol.CAR_POINT)
        setTransition(1, 'b', 137)
        setTransition(137, 'e', 138)
        setTransition(138, 'n', 139)
        setTransition(139, 'd', 140)
        setSymbol(140, Symbol.BEND)
        setTransition(92, 'i', 142)
        setTransition(142, 'n', 143)
        setTransition(143, 'e', 144)
        setSymbol(144, Symbol.LINE)
        setTransition(113, 'a', 147)
        setTransition(147, 'd', 148)
        setSymbol(148, Symbol.ROAD)
        setTransition(128, 'R', 152)
        setTransition(152, 'i', 153)
        setTransition(153, 'd', 154)
        setTransition(154, 'e', 155)
        setSymbol(155, Symbol.CAR_RIDE)
        setTransition(137, 'o', 157)
        setTransition(157, 'x', 158)
        setSymbol(158, Symbol.BOX)

        //STRING TRANSITION
        setTransition(1, '"'.code, 159)
        for (code in 0..254) {
            setTransition(159, code, 159)
        }
        setTransition(159, '"'.code, 160)
        setSymbol(160, Symbol.STRING)

        //REAL NUMBER TRANSITION
        for (digit in '0'..'9') {
            setTransition(1, digit, 161)
            setTransition(161, digit, 161)
        }
        setTransition(161, '.', 162)
        for (digit in '0'..'9') {
            setTransition(162, digit, 163)
            setTransition(163, digit, 163)
        }
        setSymbol(161, Symbol.REAL)
        setSymbol(163, Symbol.REAL)

        //VARIABLE TRANSITION
        setTransition(1, '_'.code, 164)
        for (code in 0..254) {
            setTransition(164, code, 164)
        }
        setTransition(164, '_'.code, 165)
        setSymbol(165, Symbol.VARIABLE)

        //COMMA TRANSITION
        setTransition(1, ',', 166)
        setSymbol(166, Symbol.COMMA)
    }
}

data class Token(val symbol: Symbol, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)
            if (nextState == ERROR_STATE) break // Longest match

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        last = code // The code following the current lexeme is the first code of the next lexeme

        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state)
            return if (symbol == Symbol.SKIP) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray())
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}")
        }
    }
}

fun name(symbol: Symbol) =
    when (symbol) {
        Symbol.TIMES -> "times"
        Symbol.PlUS -> "plus"
        Symbol.DIVIDES -> "divide"
        Symbol.MINUS -> "minus"
        Symbol.LPAREN -> "lparen"
        Symbol.RPAREN -> "rparen"
        Symbol.LCURLY -> "lcurly"
        Symbol.RCURLY -> "rcurly"
        Symbol.ASSIGN -> "assign"
        Symbol.SEMI_COLON -> "semi_colon"
        Symbol.COLON -> "colon"
        Symbol.IN -> "in"
        Symbol.ID -> "id"
        Symbol.CONST -> "const"
        Symbol.START -> "start"
        Symbol.FINISH -> "finish"
        Symbol.PASSENGER -> "passenger"
        Symbol.PARKING -> "parking"
        Symbol.ELECTRIC_STATION -> "electricStation"
        Symbol.GAS_STATION -> "gasStation"
        Symbol.FOREACH -> "foreach"
        Symbol.HIGHLIGHT -> "highlight"
        Symbol.LET -> "let"
        Symbol.NEIGH -> "neigh"
        Symbol.CROSS_SECTION -> "crossSection"
        Symbol.CIRC -> "circ"
        Symbol.CAR -> "car"
        Symbol.CAR_POINT -> "carPoint"
        Symbol.BEND -> "bend"
        Symbol.LINE -> "line"
        Symbol.ROAD -> "road"
        Symbol.CAR_RIDE -> "carRide"
        Symbol.BOX -> "box"
        Symbol.ROUNDABOUT -> "roundabout"
        Symbol.STRING -> "string"
        Symbol.VARIABLE -> "variable"
        Symbol.REAL -> "real"
        Symbol.COMMA -> "comma"
        else -> throw Error("Invalid symbol")
    }

fun printTokens(scanner: Scanner, output: OutputStream) {
    val writer = output.writer(Charsets.UTF_8)

    var token = scanner.getToken()
    while (token.symbol != Symbol.EOF) {
        writer.append("${name(token.symbol)}(\"${token.lexeme}\") ") // The output ends with a space!
        token = scanner.getToken()
    }
    writer.appendLine()
    writer.flush()
}