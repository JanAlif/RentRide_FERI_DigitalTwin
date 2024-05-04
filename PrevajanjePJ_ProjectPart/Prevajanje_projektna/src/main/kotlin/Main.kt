

fun main() {
    /*
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
     */

    //printTokens(Scanner(RentRideDFA, "123.35 56 * + / - ( ) { } = ; : in id \"Say somethin nice to each other\"const start finish passenger_hello_ parkingelectricStation gasStation foreach highlight let neigh crossSection roundabout circ car carPoint bend line road carRide".byteInputStream()), System.out)
    printTokens(Scanner(RentRideDFA, """carRide "voznja"{
    
    road {
        bend ((1, 1), (2, 2), 20);
        bend ((2, 2), (3, 1), 40);
        line ((3, 1), (2, 5));
        bend ((2, 5), (6, 8), 20);
        line ((6, 8), (6, 4));
    };

    car "fiat 500" {
        carPoint (0, 3)
        id :"1234";
    };

    start {(0, 4)};
    finish {(4, 5)};

    crossSection "krizisce" {
        box ((3, 4), (4, 5));
    };
    roundabout "krozni" {
        circ ((9, 4), 0.4);
    };
    gasStation "Central Charging Station" {
        (7, 3);
        let _roi_ = neigh ((3, 4), 3);
            foreach _x_ in _roi_ {
            highlight _x_
        }
    };
    electricStation "Central Gas Station" {
        (7, 3);
        let _roi_ = neigh ((3, 4), 3);
            foreach _x_ in _roi_ {
            highlight _x_
        }
    };
    parking "Nearby Parking" {
        (7, 3);
        (5, 2);
        let _roi_ = neigh ((4, 5), 3);
            foreach _x_ in _roi_ {
            highlight _x_
        }
    };
    passenger "Visitor" {
        start {(2, 2)};
        finish {(6, 8)};
    };
    passenger "Visitor" {
        start {(3, 1)};
        finish {(2, 5)};
    };
}""".byteInputStream()), System.out)

}

