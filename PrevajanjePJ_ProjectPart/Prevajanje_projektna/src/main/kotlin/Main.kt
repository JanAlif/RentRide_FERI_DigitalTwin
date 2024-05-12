import java.io.ByteArrayInputStream



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
    //println(Filter(Variable("roi"),Point(Real(4.0), Real(3.2)), Real(2.2), Foreach(Variable("x"), Variable("roi"))))
    //println(Passenger("Some passenger",Start(Point(Real(1.2), Real(3.5))), Finish(Point(Real(1.2), Real(3.5)))))
    //println(Parking("some parking", listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), Filter(Variable("roi"),Point(Real(4.0), Real(3.2)), Real(2.2), Foreach(Variable("x"), Variable("roi")))))
    //println(Electricity("elektrika", listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), Filter(Variable("roi"),Point(Real(4.0), Real(3.2)), Real(2.2), Foreach(Variable("x"), Variable("roi")))).toString())
    //println(Round(Strin("krozisce"), Circ(Point(Real(4.0), Real(3.2)),Real(22.5))))
    //println(Cross(Strin("krizisce"), Box(Point(Real(4.0), Real(3.2)),Point(Real(4.0), Real(3.2)))))
    //println( Car(Strin("some name"), Point(Real(5.0), Real(6.2)), 1234))





    /*
    val pointDeclaration = PointDeclaration(Variable("neke"),Point(Plus(Real(1.9), Real(9.1)), Real(2.2)))
    val varDecalration = VarDeclaration(Variable("neke"), Real(2.4))

    val declaration = Declarations(listOf(pointDeclaration, varDecalration))

    val l1 = Line(Point(Real(1.0), Real(2.2)), Point(Real(6.0), Real(3.2)))
    val l2 = Line(Point(Real(5.0), Real(6.2)), Point(Real(7.0), Real(8.2)))
    val b1 = Bend(Point(Real(5.0), Real(6.2)), Point(Real(7.0), Real(8.2)), 80)
    val b2 = Bend(Point(Real(5.0), Real(6.2)), Point(Real(4.0), Real(3.2)), 90)

    val path = Path(listOf(l1, l2, b1, b2))

    val car = Car(Strin("fiat 500"), Point(Real(5.0), Real(6.2)), 1234)

    val start = Start(Point(Real(1.0), Real(2.2)))
    val finish = Finish(Point(Real(4.0), Real(3.2)))

    val filter = Filter(Variable("roi"),Point(Real(4.0), Real(3.2)), Real(2.2), Foreach(Variable("x"), Variable("roi")))

    val program = CarRide(Strin("voznja"),declaration, path, car, start, finish,
        listOf(Cross(Strin("krizisce"), Box(Point(Real(4.0), Real(3.2)),Point(Real(4.0), Real(3.2)))),Cross(Strin("krizisce"), Box(Point(Real(4.0), Real(3.2)),Point(Real(4.0), Real(3.2))))),
        listOf(Round(Strin("krozisce"), Circ(Point(Real(4.0), Real(3.2)),Real(22.5))),Round(Strin("krozisce"), Circ(Point(Real(4.0), Real(3.2)),Real(22.5)))),
        Gas(Strin("Gass"), listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), filter),
        Electricity(Strin("elektrika"), listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), filter),
        Parking(Strin("Parking"), listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), filter),
        listOf( Passenger(Strin("potnik1"),Start(Point(Real(1.2), Real(3.5))), Finish(Point(Real(1.2), Real(3.5)))),Passenger(Strin("potnik2"),Start(Point(Real(1.2), Real(3.5))), Finish(Point(Real(1.2), Real(3.5)))))
    )

    println(program)


    //printTokens(Scanner(RentRideDFA, "123.35 56 * + / - ( ) { } = ; : in id \"Say somethin nice to each other\"const start finish passenger_hello_ parkingelectricStation gasStation foreach highlight let neigh crossSection roundabout circ car carPoint bend line road carRide".byteInputStream()), System.out)


    //val test = Parking("some parking", listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), Filter(Variable("roi"),Point(Real(4.0), Real(3.2)), Real(2.2), Foreach(Variable("x"), Variable("roi")))).toString()
    //val test = Passenger("Some passenger",Start(Point(Real(1.2), Real(3.5))), Finish(Point(Real(1.2), Real(3.5)))).toString()
    //val test = Gas(Strin("elektrika"), listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2))), Filter(Variable("roi"),Point(Real(4.0), Real(3.2)), Real(2.2), Foreach(Variable("x"), Variable("roi")))).toString()
    //val test = Round(Strin("krozisce"), Circ(Point(Real(4.0), Real(3.2)),Real(22.5))).toString()
    //val test = Cross(Strin("krizisce"), Box(Point(Real(4.0), Real(3.2)),Point(Real(4.0), Real(3.2)))).toString()
    //val test = Car(Strin("some name"), Point(Real(5.0), Real(6.2)), 1234).toString()
    val test = program.toString()


    //printTokens(Scanner(RentRideDFA, test.byteInputStream()), System.out)




    val scanner = Scanner(RentRideDFA, test.byteInputStream())
    val tokens = collectTokens(scanner)
    println(tokens)

    val parser = Parser(tokens)
    val isSuccess = parser.parse()

      */

    val input = "(1-1)+3*9/4"

    // Initialize the environment
    val environment = mapOf("_x_" to 1.0, "_y_" to 3.0)

    // Convert input string into a ByteArrayInputStream to simulate file input
    val inputStream = ByteArrayInputStream(input.toByteArray())

    // Create a scanner to tokenize the input using the DFA (automaton definition assumed to be correctly implemented)
    val scanner = Scanner(RentRideDFA, inputStream)
    val tokens = collectTokens(scanner)

    // Print tokens for debugging
    //tokens.forEach { token ->
       // println("Token: ${token.symbol}, Lexeme: ${token.lexeme}")
    //}

    // Create the parser with the collected tokens
    val evaluator = Evaluator(tokens)


    try {
        val ast = evaluator.evaluate()
        val result = ast.eval(environment)
        println("Result of evaluation: $result")
        println("AST to String: ${ast.toString()}")
    } catch (e: Exception) {
        println("Error parsing the expression: ${e.message}")
    }






    /*printTokens(Scanner(RentRideDFA, """
        carRide "voznja"{
    _p1_ = 0;
    _p2_ = 4;
    const _start_ = (0, 4);
    
    road {
        line ((_p1_, _p2_), (3, 4));
        bend ((3, 4), (4, 5), 90);
        line ((4, 5), (_p1_+5, _p2_+9));
    };

    car "fiat 500" {
        carPoint (0, 3);
        id :"1234";
    };

    start {start};
    finish {(4, 5)};
}
        
        
        carRide "voznja"{
    
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

     */

}

