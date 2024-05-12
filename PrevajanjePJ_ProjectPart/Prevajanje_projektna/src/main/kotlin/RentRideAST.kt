interface RentRide{
    override fun toString(): String
}
interface PathSegment{
    override fun toString(): String
}
interface VarPointDeclarations{
    override fun toString(): String
}


interface Expr {
    override fun toString(): String
    fun eval(env: Map<String, Double>): Double
}
interface Str {
    override fun toString(): String
}


data class CarRide(
    val str: Strin,
    val decl: Declarations? = null,
    val road: Path,
    val car: Car,
    val start: Start,
    val finish: Finish,
    val cross: List<Cross?>? = null,
    val round: List<Round?>? = null,
    val gas: Gas? = null,
    val electricity: Electricity? = null,
    val parking: Parking? = null,
    val passenger: List<Passenger?>? = null): RentRide{
    override fun toString(): String{
        var str = "carRide ${str.toString()} {\n"
        str += decl.toString()+"\n"
        str += road.toString()+"\n"
        str += car.toString()+"\n"
        str += start.toString()+"\n"
        str += finish.toString()+"\n"

        if (cross != null) {
            for(cros in cross){
                str += cros.toString() +"\n"
            }
        }
        if (round != null) {
            for(roun in round){
                str += roun.toString() +"\n"
            }
        }

        str += gas.toString()+"\n"
        str += electricity.toString()+"\n"
        str += parking.toString()+"\n"
        if (passenger != null) {
            for(pass in passenger){
                str += pass.toString() +"\n"
            }
        }
        str += "\n}"
        return str
    }
    }
data class Declarations(val decl: List<VarPointDeclarations>): RentRide{
    override fun toString(): String{
        var str = ""
        for(segment in decl){
            str += segment.toString() +"\n"
        }
        return str
    }
}
data class PointDeclaration(val variable:Variable, val point: Point): VarPointDeclarations{
    override fun toString() = "const ${variable.toString()} = ${point.toString()};"
}
data class VarDeclaration(val variable:Variable, val expr: Expr): VarPointDeclarations{
    override fun toString() = "${variable.toString()} = ${expr.toString()};"
}
data class Path(val segments: List<PathSegment>): RentRide{
    override fun toString(): String{
        var str = " road {\n"
        for(segment in segments){
            str += segment.toString()
        }
        str += "};"
        return str
    }
}
data class Line(val p1:Point, val p2:Point): PathSegment{
    override fun toString() = "\t line (${p1.toString()}, ${p2.toString()} );\n"
}
data class Bend(val p1: Point, val p2: Point, val curve: Int): PathSegment{
    override fun toString() = "\t bend (${p1.toString()}, ${p2.toString()}, ${curve.toString()});\n"
}
data class Car(val str: Strin, val point: Point, val id: Int): RentRide{
    override fun toString() = " car ${str.toString()} {\n\t carPoint${point.toString()}; \n\t id:${id.toString()}\n};"
}
data class Start(val point: Point) : RentRide{
    override fun toString() = " start { ${point.toString()} };"
}
data class Finish(val point: Point) : RentRide{
    override fun toString() = " finish { ${point.toString()} };"
}
data class Cross(val str: Strin, val box: Box): RentRide{
    override fun toString() = " crossSection ${str.toString()} {\n\t ${box.toString()} \n};"
}
data class Box(val p1: Point, val p2: Point): RentRide{
    override fun toString() = " box ( ${p1.toString()}, ${p2.toString()} );"
}
data class Round(val str: Strin, val circ: Circ): RentRide{
    override fun toString() = " roundabout ${str.toString()} {\n\t ${circ.toString()} \n};"
}
data class Circ(val point: Point, val expr: Expr): RentRide{
    override fun toString() = " circ ( ${point.toString()}, ${expr.toString()} );"
}
data class Gas(val str: Strin, val points: List<Point>, val filter: Filter): RentRide{
    override fun toString(): String{
        var str = " gasStation ${str.toString()} {\n"
        for(point in points){
            str += "${point.toString()};\n"
        }
        str += "$filter \n}; \n"
        return str
    }
}
data class Electricity(val str: Strin, val points: List<Point>, val filter: Filter): RentRide{
    override fun toString(): String{
        var str = " electricStation ${str.toString()} {\n"
        for(point in points){
            str += "${point.toString()};\n"
        }
        str += "$filter\n }; \n"
        return str
    }
}
data class Parking(val str: Strin, val points: List<Point>, val filter: Filter) : RentRide{
    override fun toString(): String{
        var str = " parking ${str.toString()} {\n"
        for(point in points){
            str += "${point.toString()};\n"
        }
        str += "$filter\n }; \n"
        return str
    }
}
data class Point(val p1: Expr, val p2: Expr) : RentRide{
    override fun toString() = "( ${p1.toString()}, ${p2.toString()} )"
}
data class Passenger(val str: Strin, val start: Start, val finish: Finish) : RentRide{
    override fun toString() = " passenger ${str.toString()} { \n\t${start.toString()} \n\t${finish.toString()}\n};"
}
data class Filter(val variable: Variable, val point: Point, val expr: Expr, val foreach: Foreach): RentRide{
    override fun toString() = "let ${variable.toString()} = neigh ( ${point.toString()} , ${expr.toString()} ); \n ${foreach.toString()}"
}
data class Foreach(val str: Variable, val roi: Variable) : RentRide{
    override fun toString() = "foreach ${str.toString()} in ${roi.toString()} { \n \t highlight ${str.toString()}\n }"
}
data class Plus(val left: Expr, val right: Expr) : Expr {
    override fun toString() = "(${left.toString()} + ${right.toString()})"
    override fun eval(env: Map<String, Double>) = left.eval(env) + right.eval(env)
}
data class Minus(val left: Expr, val right: Expr) : Expr {
    override fun toString() = "(${left.toString()} - ${right.toString()})"
    override fun eval(env: Map<String, Double>) = left.eval(env) - right.eval(env)
}
data class Times(val left: Expr, val right: Expr) : Expr {
    override fun toString() = "(${left.toString()} * ${right.toString()})"
    override fun eval(env: Map<String, Double>) = left.eval(env) * right.eval(env)
}
data class Divides(val left: Expr, val right: Expr) : Expr {
    override fun toString() = "(${left.toString()} / ${right.toString()})"
    override fun eval(env: Map<String, Double>) = left.eval(env) / right.eval(env)
}
data class UnaryPlus(val expr: Expr) : Expr {
    override fun toString() = "(+${expr.toString()})"
    override fun eval(env: Map<String, Double>) = expr.eval(env)
}
data class UnaryMinus(val expr: Expr) : Expr {
    override fun toString() = "(-${expr.toString()})"
    override fun eval(env: Map<String, Double>) = -expr.eval(env)
}
data class Real(val value: Double) : Expr {
    override fun toString() = value.toString()
    override fun eval(env: Map<String, Double>) = value
}
data class Variable(val name: String) : Expr {
    override fun toString() = "_${name}_"
    override fun eval(env: Map<String, Double>) = env[name] ?: error("Variable $name not found")
}

data class Strin(val str: String) : Str{
    override fun toString() = "\"${str}\""
}