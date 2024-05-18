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
        Gas(Strin("Gass"), Points(listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2)))), filter),
        Electricity(Strin("elektrika"), Points(listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2)))), filter),
        Parking(Strin("Parking"), Points(listOf(Point(Real(4.0), Real(3.2)), Point(Real(6.0), Real(8.4)), Point(Minus(Plus(Variable("x"), Variable("y")), Plus(Real(2.0), Real(2.0))), Real(12.2)), Point(Real(22.5), Real(3.2)))), filter),
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
    println(isSuccess)

 */

















    val input = """carRide "voznja"{
        
        const _start_ = (0, 4);
        _p2_ = 4 +1;
        
    road {
        bend ((_p2_+2, 1), (2, 2), 20);
        bend ((2, 2), (3, 1), 40);
        line ((3, 1), (2, 5));
        bend ((2, 5), (6, 8), 20);
        line ((6, 8), (6, 4));
        line ((6, 4), (0, 0));
    };

    car "fiat 500" {
        carPoint (0, 3);
        id : 1234
    };

    start {_start_};
    finish {(4, 5)};
    
    
     crossSection "krizisce" {
        box ((3, 4), (4, 5));
    };
    crossSection "krizisce" {
        box ((4, 5), (5, 6));
    };
    roundabout "krozni" {
        circ ((9, 4), 0.4);
    };
    roundabout "krozni" {
        circ ((9, 4), 0.4);
    };
    
    gasStation "Central Charging Station" {
        (7, 3);
        (8, 9);
        (4, 6);
        (3, 3);
        let _roi_ = neigh ((3, 4), 3);
            foreach _x_ in _roi_ {
            highlight _x_
        }
    };
    
   electricStation "Central Gas Station" {
        (5, 3);
        (1, 2);
        (3, 3);
        (4, 4);
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
}
"""


    val input1 = """
        carRide "Primer voznje"{
    const _start_ = (15.640382686482809, 46.558408734975075);
    const _finish_ = (15.650584568603904, 46.55491140270988);
    const _car_ = (15.653110184408348, 46.56304997215511);

    road {
        line(_start_, (15.641406686338712, 46.55823270533739));
        line((15.641406686338712, 46.55823270533739), (15.641689821864617, 46.55907480446848));
        line((15.641689821864617, 46.55907480446848), (15.643412739947195, 46.55886474311487));
        bend ((15.643412739947195, 46.55886474311487), (15.645089863995395,46.55885862540842), 40);
        bend ((15.645089863995395,46.55885862540842), (15.645547483015662,46.559115191425406), 160);
        bend ((15.645547483015662,46.559115191425406), (15.645027359272262, 46.55945042012158), 240);
        bend ((15.645027359272262, 46.55945042012158), (15.6444104987375, 46.559477727573466), 30);
        line((15.6444104987375, 46.559477727573466), (15.64437193128586,46.560190278483304));
        bend ((15.64437193128586,46.560190278483304), (15.644416752102842, 46.560337275148555), 30);
        line((15.644416752102842, 46.560337275148555), (15.644305814606412,46.56299992488334));
        line((15.644305814606412,46.56299992488334), (15.646431138589094,46.56301566246833));
        line((15.646431138589094,46.56301566246833), (15.648214240320812, 46.56300693037906));
        line((15.648214240320812, 46.56300693037906), (15.653110184408348, 46.56304997215511));
        line((15.653110184408348, 46.56304997215511), (15.65318584372028, 46.56044705545702));
        line((15.65318584372028, 46.56044705545702), (15.651723889534566, 46.56037079702935));
        bend ((15.651723889534566, 46.56037079702935), (15.651166498520183, 46.559740476226494), 250);
        line((15.651166498520183, 46.559740476226494), (15.651008498691056, 46.55826414242625));
        bend ((15.651008498691056, 46.55826414242625), (15.650866018874297, 46.558041287035365), 550);
        bend ((15.650866018874297, 46.558041287035365), (15.650944713727569, 46.557815535783874), 10);
        line((15.650944713727569, 46.557815535783874), (15.650584568603904, 46.55491140270988));
    };

    car "Audi RS7" {
        carPoint _car_;
        id : 1
    };

    start {_start_};
    finish {_finish_};
    
    crossSection "krizisce" {
        box ((15.641401601628502, 46.55824330946504), (15.641432046440968, 46.558213357209894));
    };
    crossSection "krizisce" {
        box ((15.641668903810142, 46.559095597688525), (15.641707312122293, 46.55904938053982));
    };
    crossSection "krizisce" {
        box ((15.64433740358757, 46.56021552405551), (15.644417484338106, 46.56017137452699));
    };
    crossSection "krizisce" {
        box ((15.64433012396637, 46.561147603282194), (15.644406803594848, 46.56110241179283));
    };
    crossSection "krizisce" {
        box ((15.644264732080302, 46.56202817400904), (15.644390684655889, 46.561938242052264));
    };
    crossSection "krizisce" {
        box ((15.644260045797239, 46.56302583619933), (15.64434628361542, 46.56295259142348));
    };
    crossSection "krizisce" {
        box ((15.646428838434701, 46.563070638420754), (15.646550156150738, 46.56296283135356));
    };
    crossSection "krizisce" {
        box ((15.64817185245522, 46.56304756310266), (15.64827618444491, 46.562980833683184));
    };
    crossSection "krizisce" {
        box ((15.649954759572438, 46.56306642881822), (15.650043403902004, 46.56299462711647));
    };
    crossSection "krizisce" {
        box ((15.649954759572438, 46.56306642881822), (15.650043403902004, 46.56299462711647));
    };  
    crossSection "krizisce" {
        box ((15.65159043608611, 46.563078826472974), (15.65168788062283, 46.563003679417136));
    }; 
    crossSection "krizisce" {
        box ((15.653055955136267, 46.563097309356806), (15.653168642050787, 46.56302225201762));
    }; 
    crossSection "krizisce" {
        box ((15.653078101030303, 46.56210252964149), (15.653216772193133, 46.56201641074418));
    }; 
    crossSection "krizisce" {
        box ((15.653092540719797, 46.560528246977015), (15.653316203885396, 46.560402140600075));
    }; 
    crossSection "krizisce" {
        box ((15.650945459117963, 46.56041815302595), (15.651907210734237, 46.559935255308545));
    }; 
    
    roundabout "krozni" {
        circ ((15.651052512423945, 46.558031111426374), 0.015);
    };
    
    gasStation "Central Charging Station" {
        (15.65291825164445, 46.562585962324704);
        (15.656933511609736, 46.5653466315168);
        (15.661973646363975, 46.560596357821794);
        (15.668655798005773, 46.56935621786192);
        let _roi_ = neigh (_car_, 0.005);
            foreach _x_ in _roi_ {
            highlight _x_
        }
    };
    
    parking "Nearby Parking" {
        (15.653220458934385, 46.553807344119406);
        (15.647169533881964, 46.55477154228376);
        (15.649296703165675, 46.548273299910335);
        (15.650473913544062, 46.53844550429912);
        let _roi_ = neigh (_finish_, 0.004);
            foreach _x_ in _roi_ {
            highlight _x_
        }
    };
    
    passenger "Visitor" {
        start {(15.64385910744997, 46.558854974272606)};
        finish {(15.653184937344918, 46.5605020082904)};
    };
}

"""
    // Initialize the environment
    val environment = mapOf("_x_" to 1.9, "_y_" to 3.0, "_point_" to Point(Real(5.0), Real(6.2)))

    // Convert input string into a ByteArrayInputStream to simulate file input
    val inputStream = ByteArrayInputStream(input1.toByteArray())

    // Create a scanner to tokenize the input using the DFA (automaton definition assumed to be correctly implemented)
    val scanner = Scanner(RentRideDFA, inputStream)
    val tokens = collectTokens(scanner)

    // Print tokens for debugging
    //tokens.forEach { token ->
      //  println("Token: ${token.symbol}, Lexeme: ${token.lexeme}")
    //}

    // Create the parser with the collected tokens
    val evaluator = Evaluator(tokens)


    try {
        val ast = evaluator.evaluate()
        val electricPoints = evaluator.electricPoints
        val gasPoints = evaluator.gasPoints
        //val generalPoints = evaluator.generalPoints
        val parkingPoints = evaluator.parkingPoints
        val envir = evaluator.varPointEnv
        val result = ast.eval(envir, parkingPoints, electricPoints, gasPoints)
        println("Result of evaluation: $result")
        //println("AST to String: ${ast.toString()}")
    } catch (e: Exception) {
        println("Error parsing the expression: ${e.message}")
    }





/*
    printTokens(Scanner(RentRideDFA, """
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

