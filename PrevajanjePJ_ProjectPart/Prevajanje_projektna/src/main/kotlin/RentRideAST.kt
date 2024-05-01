interface RentRide{}
interface Expr {}
interface PathSegment{}
interface VarPointDeclarations{}


data class CarRide(val decl: Declarations? = null,
                   val road: Path,
                   val car: Car,
                   val start: Start,
                   val finish: Finish,
                   val cross: Cross? = null,
                   val round: Round? = null,
                   val gas: Gas? = null,
                   val electricity: Electricity? = null,
                   val parking: Parking? = null,
                   val passenger: Passenger? = null){}
data class Declarations(val decl: List<VarPointDeclarations>): RentRide{}
data class PointDeclaration(val point: Point): VarPointDeclarations{}
data class VarDeclaration(val expr: Expr): VarPointDeclarations{}
data class Path(val segments: List<PathSegment>): RentRide{}
data class Line(val p1:Point, val p2:Point): PathSegment{}
data class Bend(val p1: Point, val p2: Point, val curve: Int): PathSegment{}
data class Car(val str: String, val point: Point, val id: Int): RentRide{}
data class Start(val point: Point) : RentRide{}
data class Finish(val point: Point) : RentRide{}
data class Cross(val str: String, val box: Box): RentRide{}
data class Box(val p1: Point, val p2: Point): RentRide{}
data class Round(val str: String, val circ: Circ): RentRide{}
data class Circ(val point: Point, val expr: Expr): RentRide{}
data class Gas(val str: String, val points: List<Point>, val filter: Filter): RentRide{}
data class Electricity(val str: String, val points: List<Point>, val filter: Filter): RentRide{}
data class Parking(val str: String, val points: List<Point>, val filter: Filter) : RentRide{}
data class Point(val p1: Expr, val p2: Expr) : RentRide{}
data class Passenger(val str: String, val start: Start, val finish: Finish) : RentRide{}
data class Filter(val point: Point, val expr: Expr, val foreach: Foreach): RentRide{}
data class Foreach(val str: String, val roi: String) : RentRide{}

data class Plus(val left: Expr, val right: Expr) : Expr {}

data class Minus(val left: Expr, val right: Expr) : Expr {}

data class Times(val left: Expr, val right: Expr) : Expr {}

data class Divides(val left: Expr, val right: Expr) : Expr {}

data class UnaryPlus(val expr: Expr) : Expr {}

data class UnaryMinus(val expr: Expr) : Expr {}

data class Real(val value: Double) : Expr {}

data class Variable(val name: String) : Expr {}