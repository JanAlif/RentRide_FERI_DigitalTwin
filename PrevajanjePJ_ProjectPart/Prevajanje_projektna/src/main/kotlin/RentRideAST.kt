import kotlin.math.*

interface RentRide {
    override fun toString(): String
}

interface PathSegment {
    override fun toString(): String
    fun eval(env: Map<String, Any>): String
}

interface VarPointDeclarations {
    override fun toString(): String
    fun eval(env: MutableMap<String, Any>)
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
    val decl: Map<String, Any> = emptyMap(),
    val road: Path,
    val car: Car,
    val start: Start,
    val finish: Finish,
    val cross: List<Cross?>? = null,
    val round: List<Round?>? = null,
    val gas: Gas? = null,
    val electricity: Electricity? = null,
    val parking: Parking? = null,
    val passenger: List<Passenger?>? = null
) : RentRide {
    override fun toString(): String {
        var str = "carRide ${str.toString()} {\n"
        decl?.let { str += it.toString() + "\n" }
        str += road.toString() + "\n"
        str += car.toString() + "\n"
        str += start.toString() + "\n"
        str += finish.toString() + "\n"

        cross?.forEach { it?.let { str += it.toString() + "\n" } }
        round?.forEach { it?.let { str += it.toString() + "\n" } }

        gas?.let { str += it.toString() + "\n" }
        electricity?.let { str += it.toString() + "\n" }
        parking?.let { str += it.toString() + "\n" }

        passenger?.forEach { it?.let { str += it.toString() + "\n" } }

        str += "\n}"
        return str
    }

    fun eval(env: Map<String, Any>, parkingPoints: List<Point>, electricityPoints: List<Point>, gasPoints: List<Point>): String {
        //printVariables(decl)
        val features = mutableListOf<String>()
        //decl?.let { it.eval(env as MutableMap<String, Any>) }
        features.add(road.eval(env))
        features.add(car.eval(env))
        features.add(start.eval(env))
        features.add(finish.eval(env))

        cross?.forEach { it?.let { features.add(it.eval(env)) } }
        round?.forEach { it?.let { features.add(it.eval(env)) } }

        gas?.let { features.add(it.eval(env, gasPoints)) }
        electricity?.let { features.add(it.eval(env, electricityPoints)) }
        parking?.let { features.add(it.eval(env, parkingPoints)) }

        passenger?.forEach { it?.let { features.add(it.eval(env)) } }

        return """
        {
            "type": "FeatureCollection",
            "features": [
                ${features.joinToString(",\n")}
            ]
        }
        """
    }
}

data class Declarations(val decl: List<VarPointDeclarations>) : RentRide {
    override fun toString(): String {
        var str = ""
        for (segment in decl) {
            str += segment.toString() + "\n"
        }
        return str
    }

    fun eval(env: MutableMap<String, Any>) {
        decl.forEach { it.eval(env) }
    }
}

data class PointDeclaration(val variable: Variable, val point: Point) : VarPointDeclarations {
    override fun toString() = "const ${variable.toString()} = ${point.toString()};"

    override fun eval(env: MutableMap<String, Any>) {
        env[variable.name] = point
    }
}

data class VarDeclaration(val variable: Variable, val expr: Expr) : VarPointDeclarations {
    override fun toString() = "${variable.toString()} = ${expr.toString()};"
    override fun eval(env: MutableMap<String, Any>) {
        env[variable.name] = expr.eval(env as Map<String, Double>)
    }
}

data class Path(val segments: List<PathSegment>) : RentRide {
    override fun toString(): String {
        var str = " road {\n"
        for (segment in segments) {
            str += segment.toString()
        }
        str += "};"
        return str
    }

    fun eval(env: Map<String, Any>): String {
        val features = segments.map { it.eval(env) }.joinToString(",\n")
        return """
        
            
                $features
          
        """
    }
}

data class Line(val p1: Point, val p2: Point) : PathSegment {
    override fun toString() = "\t line (${p1.toString()}, ${p2.toString()} );\n"

    override fun eval(env: Map<String, Any>): String {
        val coordinates = """
            [
                [${p1.p1.eval(env as Map<String, Double>)}, ${p1.p2.eval(env)}],
                [${p2.p1.eval(env)}, ${p2.p2.eval(env)}]
            ]
        """
        val geoJson = """
        {
            "type": "Feature",
            "properties": {
                "name": "line",
                "stroke": "#0000ff",
                "stroke-width": 2,
                "stroke-opacity": 0.8
            },
            "geometry": {
                "type": "LineString",
                "coordinates": $coordinates
            }
        }
        """
        return geoJson
    }
}

data class Bend(val p1: Point, val p2: Point, val curve: Int) : PathSegment {
    override fun toString() = "\t bend (${p1.toString()}, ${p2.toString()}, ${curve.toString()});\n"

    override fun eval(env: Map<String, Any>): String {
        val x1 = p1.p1.eval(env as Map<String, Double>)
        val y1 = p1.p2.eval(env)
        val x2 = p2.p1.eval(env)
        val y2 = p2.p2.eval(env)

        // Midpoint
        val midX = (x1 + x2) / 2
        val midY = (y1 + y2) / 2

        // Perpendicular offset for the curve (scaled by 'curve')
        val dx = x2 - x1
        val dy = y2 - y1
        val dist = sqrt(dx * dx + dy * dy)
        var offsetX = 0.0
        var offsetY = 0.0
        if (curve > 500){
            offsetX = -(curve-500) * dy / dist / 1000000  // Scale down the offset for gentler curve
            offsetY = (curve-500) * dx / dist / 1000000  // Scale down the offset for gentler curve
        }else{
            offsetX = curve * dy / dist / 1000000  // Scale down the offset for gentler curve
            offsetY = -curve * dx / dist / 1000000  // Scale down the offset for gentler curve
        }


        val controlX = midX + offsetX
        val controlY = midY + offsetY

        // Number of segments to approximate the curve
        val numSegments = 128
        val tIncrement = 1.0 / numSegments
        val points = (0..numSegments).map { i ->
            val t = i * tIncrement
            val x = (1 - t).pow(2) * x1 + 2 * (1 - t) * t * controlX + t.pow(2) * x2
            val y = (1 - t).pow(2) * y1 + 2 * (1 - t) * t * controlY + t.pow(2) * y2
            "[$x, $y]"
        }

        val coordinates = points.joinToString(", ")

        val geoJson = """
        {
            "type": "Feature",
            "properties": {
                "name": "bend",
                "stroke": "#ff0000",
                "stroke-width": 2,
                "stroke-opacity": 0.8
            },
            "geometry": {
                "type": "LineString",
                "coordinates": [$coordinates]
            }
        }
        """
        return geoJson
    }
}

data class Car(val str: Strin, val point: Point, val id: Int) : RentRide {
    override fun toString() = " car ${str.toString()} {\n\t carPoint${point.toString()}; \n\t id:${id.toString()}\n};"

    fun eval(env: Map<String, Any>): String {
        val properties = """
            "properties": {
                "marker-color": "#0000ff",
                "marker-size": "large",
                "marker-symbol": "car",
                "name": ${str.str},
                "id": $id
            },
        """
        val geoJson = """
        {
            "type": "Feature",
            $properties
            "geometry": {
                "type": "Point",
                "coordinates": [
                    ${point.p1.eval(env as Map<String, Double>)},
                    ${point.p2.eval(env)}
                ]
            }
        }
        """
        return geoJson
    }
}

data class Start(val point: Point) : RentRide {
    override fun toString() = " start { ${point.toString()} };"

    fun eval(env: Map<String, Any>, type: String = ""): String {
        val properties = if (type == "pass") {
            """
                "properties": {
                    "marker-color": "#ffee00",
                    "marker-size": "large",
                    "marker-symbol": "star",
                    "name": "start"
                },
            """
        } else {
            """
                "properties": {
                    "marker-color": "#ff0000",
                    "marker-size": "large",
                    "marker-symbol": "star",
                    "name": "start"
                },
            """
        }

        val geoJson = """
        {
            "type": "Feature",
            $properties
            "geometry": {
                "type": "Point",
                "coordinates": [
                    ${point.p1.eval(env as Map<String, Double>)},
                    ${point.p2.eval(env)}
                ]
            }
        }
        """
        return geoJson
    }
}

data class Finish(val point: Point) : RentRide {
    override fun toString() = " finish { ${point.toString()} };"

    fun eval(env: Map<String, Any>, type: String = ""): String {

        val properties = if (type == "pass") {
            """
                "properties": {
                    "marker-color": "#00eeff",
                    "marker-size": "large",
                    "marker-symbol": "star",
                    "name": "finish"
                },
            """
        } else {
            """
                "properties": {
                    "marker-color": "#0000ff",
                    "marker-size": "large",
                    "marker-symbol": "star",
                    "name": "finish"
                },
            """
        }

        val geoJson = """
        {
            "type": "Feature",
            $properties
            "geometry": {
                "type": "Point",
                "coordinates": [
                    ${point.p1.eval(env as Map<String, Double>)},
                    ${point.p2.eval(env)}
                ]
            }
        }
        """
        return geoJson
    }
}

data class Cross(val str: Strin, val box: Box) : RentRide {
    override fun toString() = " crossSection ${str.toString()} {\n\t ${box.toString()} \n};"

    fun eval(env: Map<String, Any>): String {
        return box.eval(env)
    }
}

data class Box(val p1: Point, val p2: Point) : RentRide {
    override fun toString() = " box ( ${p1.toString()}, ${p2.toString()} );"

    fun eval(env: Map<String, Any>): String {
        val x1 = p1.p1.eval(env as Map<String, Double>)
        val y1 = p1.p2.eval(env)
        val x2 = p2.p1.eval(env)
        val y2 = p2.p2.eval(env)

        // Define the four corners of the box
        val bottomLeft = "[$x1, $y1]"
        val topRight = "[$x2, $y2]"
        val topLeft = "[$x1, $y2]"
        val bottomRight = "[$x2, $y1]"

        // Create the coordinates list in GeoJSON format
        val coordinates = listOf(bottomLeft, topLeft, topRight, bottomRight, bottomLeft).joinToString(", ")

        val geoJson = """
        {
            "type": "Feature",
            "properties": {
                "name": "box",
                "fill": "#ff0000",
                "fill-opacity": 0.5,
                "stroke": "#ff0000",
                "stroke-width": 2,
                "stroke-opacity": 0.8
            },
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        $coordinates
                    ]
                ]
            }
        }
        """
        return geoJson
    }
}

data class Round(val str: Strin, val circ: Circ) : RentRide {
    override fun toString() = " roundabout ${str.toString()} {\n\t ${circ.toString()} \n};"

    fun eval(env: Map<String, Any>): String {
        return circ.eval(env)
    }
}

data class Circ(val point: Point, val expr: Expr) : RentRide {
    override fun toString() = " circ ( ${point.toString()}, ${expr.toString()} );"

    fun eval(env: Map<String, Any>): String {
        val centerX = point.p1.eval(env as Map<String, Double>)
        val centerY = point.p2.eval(env)
        val radius = expr.eval(env)

        // Constants
        val earthRadius = 6371.0  // Radius of the Earth in kilometers
        val circleRadius = radius / earthRadius  // Circle radius in radians

        // Convert center coordinates to radians
        val lat = Math.toRadians(centerY)
        val lon = Math.toRadians(centerX)

        // Generate points around the circle's circumference
        val points = (0 until 360 step 10).map { i ->
            val beta = Math.toRadians(i.toDouble())
            val lat_ = asin(sin(lat) * cos(circleRadius) + cos(lat) * sin(circleRadius) * cos(beta))
            val lon_ = lon + atan2(sin(beta) * sin(circleRadius) * cos(lat), cos(circleRadius) - sin(lat) * sin(lat_))
            val x = Math.toDegrees(lon_)
            val y = Math.toDegrees(lat_)
            "[$x, $y]"
        }

        // Add the first point again at the end to close the polygon
        val firstPoint = points.first()
        val coordinates = (points + firstPoint).joinToString(", ")

        val geoJson = """
        {
            "type": "Feature",
            "properties": {
                "name": "circle",
                "fill": "#00ff00",
                "fill-opacity": 0.5,
                "stroke": "#00ff00",
                "stroke-width": 2,
                "stroke-opacity": 0.8
            },
            "geometry": {
                "type": "Polygon",
                "coordinates": [
                    [
                        $coordinates
                    ]
                ]
            }
        }
        """
        return geoJson
    }
}

data class Gas(val str: Strin, val points: Points, val filter: Filter) : RentRide {
    override fun toString(): String {
        var str = " gasStation ${str.toString()} {\n"
        str += points.toString()
        str += "$filter \n}; \n"
        return str
    }

    fun eval(env: Map<String, Any>, gasPoints: List<Point>): String {
        val highlightedPoints = filter.eval(env, gasPoints, "gas")
        val geoJsonFeatures = highlightedPoints.joinToString(",\n") { it.eval(env) }
        return """
        
                $geoJsonFeatures
           
        """
    }
}

data class Electricity(val str: Strin, val points: Points, val filter: Filter) : RentRide {
    override fun toString(): String {
        var str = " electricStation ${str.toString()} {\n"
        str += points.toString()
        str += "$filter\n }; \n"
        return str
    }

    fun eval(env: Map<String, Any>, electricityPoints: List<Point>): String {
        val highlightedPoints = filter.eval(env, electricityPoints, "electric")
        val geoJsonFeatures = highlightedPoints.joinToString(",\n") { it.eval(env as Map<String, Double>) }
        return """
        
                $geoJsonFeatures
           
        """
    }
}

data class Parking(val str: Strin, val points: Points, val filter: Filter) : RentRide {
    override fun toString(): String {
        var str = " parking ${str.toString()} {\n"
        str += points.toString()
        str += "$filter\n }; \n"
        return str
    }

    fun eval(env: Map<String, Any>, parkingPoints: List<Point>): String {
        val highlightedPoints = filter.eval(env, parkingPoints, "parking")
        val geoJsonFeatures = highlightedPoints.joinToString(",\n") { it.eval(env) }
        return """
        
                $geoJsonFeatures
        """
    }
}

data class Points(val points: List<Point>, val type: String = "") : RentRide {
    override fun toString(): String {
        var str = ""
        for (point in points) {
            str += "$point; \n"
        }
        return str
    }

    fun eval(env: Map<String, Double>): String {
        var geoJson = ""
        for (point in points) {
            geoJson += point.eval(env) + ",\n"
        }
        geoJson = geoJson.substring(0, geoJson.length - 2)
        return geoJson
    }
}

data class Point(val p1: Expr, val p2: Expr, val within: Boolean = false, val type: String = "") : RentRide {
    override fun toString() = "( ${p1.toString()}, ${p2.toString()} )"

    fun eval(env: Map<String, Any>): String {
        var icon = ""
        if(type == "parking"){
            icon = "\"marker-symbol\": \"parking\""
        }else if(type == "gas"){
            icon = "\"marker-symbol\": \"fuel\""
        }else if(type == "electric"){
            icon = "\"marker-symbol\": \"charging-station\""
        }

        val properties = if (within && type == "parking") {
            """
                "properties": {
                    "marker-color": "#0000ff",
                    "marker-size": "large",
                    "marker-symbol": "parking",
                    "name": "parking"
                },
            """
        } else if (within && type == "gas") {
            """
                "properties": {
                    "marker-color": "#ffaa00",
                    "marker-size": "large",
                    "marker-symbol": "fuel",
                    "name": "gas"
                },
            """
        } else if (within && type == "electric") {
            """
                "properties": {
                    "marker-color": "#00ff00",
                    "marker-size": "large",
                    "marker-symbol": "charging-station",
                    "name": "electric"
                },
            """
        } else {
            """
                "properties": {
                $icon
                },
            """
        }

        val geoJson = "{\n" +
                "      \"type\": \"Feature\",\n" +
                "      $properties" +
                "      \"geometry\": {\n" +
                "        \"coordinates\": [\n" +
                "          ${p1.eval(env as Map<String, Double>)},\n" +
                "          ${p2.eval(env as Map<String, Double>)}\n" +
                "        ],\n" +
                "        \"type\": \"Point\"\n" +
                "      }\n" +
                "   }"
        return geoJson
    }
}

data class Passenger(val str: Strin, val start: Start, val finish: Finish) : RentRide {
    override fun toString() = " passenger ${str.toString()} { \n\t${start.toString()} \n\t${finish.toString()}\n};"

    fun eval(env: Map<String, Any>): String {
        val startGeoJson = start.eval(env, "pass")
        val finishGeoJson = finish.eval(env, "pass")
        return """
        
                $startGeoJson,
                $finishGeoJson
         
        """
    }
}

data class Filter(val variable: Variable, val point: Point, val expr: Expr, val foreach: Foreach) : RentRide {
    override fun toString() = "let ${variable.toString()} = neigh ( ${point.toString()} , ${expr.toString()} ); \n ${foreach.toString()}"

    fun eval(env: Map<String, Any>, points: List<Point>, type: String = ""): List<Point> {
        val center = Point(point.p1, point.p2)
        val radius = expr.eval(env as Map<String, Double>)
        return foreach.eval(env, points, type, center, radius)
    }
}

data class Foreach(val str: Variable, val roi: Variable) : RentRide {
    override fun toString() = "foreach ${str.toString()} in ${roi.toString()} { \n \t highlight ${str.toString()}\n }"

    fun eval(env: Map<String, Any>, points: List<Point>, type: String = "", center: Point, radius: Double): List<Point> {
        return points.map { point ->
            if (getPointWithinRadius(point, center, radius, env)) {
                point.copy(within = true, type = type)
            } else {
                point
            }
        }
    }
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

data class Strin(val str: String) : Str {
    override fun toString() = "\"${str}\""
}

fun getPointWithinRadius(point: Point, center: Point, radius: Double, env: Map<String, Any>): Boolean {
    val centerX = center.p1.eval(env as Map<String, Double>)
    val centerY = center.p2.eval(env)

    val x = point.p1.eval(env)
    val y = point.p2.eval(env)
    val distance = sqrt((x - centerX).pow(2.0) + (y - centerY).pow(2.0))
    return distance <= radius
}

fun printVariables(variables: Map<String, Any>) {
    for ((name, value) in variables) {
        println("Variable Name: $name, Value: $value")
    }
}

