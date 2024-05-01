

fun main() {
    val passenger = Passenger("Some passenger",Start(Point(Real(1.2), Real(3.5))), Finish(Point(Real(1.2), Real(3.5))))

    val plus = Plus(Real(1.9), Real(9.1))
    val pointDeclaration = PointDeclaration(Point(plus, Real(2.2)))
    val varDecalration = VarDeclaration(plus)

    val declaration = Declarations(listOf(pointDeclaration, varDecalration))

    val l1 = Line(Point(Real(1.0), Real(2.2)), Point(Real(6.0), Real(3.2)))
    val l2 = Line(Point(Real(5.0), Real(6.2)), Point(Real(7.0), Real(8.2)))
    val b1 = Bend(Point(Real(5.0), Real(6.2)), Point(Real(7.0), Real(8.2)), 80)
    val b2 = Bend(Point(Real(5.0), Real(6.2)), Point(Real(4.0), Real(3.2)), 90)

    val path = Path(listOf(l1, l2, b1, b2))

    val car = Car("some name", Point(Real(5.0), Real(6.2)), 1234)

    val start = Start(Point(Real(1.0), Real(2.2)))
    val finish = Finish(Point(Real(4.0), Real(3.2)))

    val program = CarRide(declaration, path, car, start, finish)
}
