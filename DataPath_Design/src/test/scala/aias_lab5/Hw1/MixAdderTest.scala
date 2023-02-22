package aias_lab5.Hw1

import chisel3.iotesters.{Driver,PeekPokeTester}

class MixAdderTest (dut:MixAdder) extends PeekPokeTester(dut){
    var pass = 0
    for (i <- 0 until 1000){ //FIXME:1000
        val x = rnd.nextInt(1<<30) //FIXME:1<<30
        val y = rnd.nextInt(1<<30) //FIXME:1<<30

        poke(dut.io.in1,x) // 0000_0000_0000_0000_0000_0000_0000_0011  // 5
        poke(dut.io.in2,y) // 0000_0000_0000_0000_0000_0000_0000_0100 // 8
        
        if(!expect(dut.io.Sum,x+y)){
            pass = pass + 1
            println("in1 = " + x)
            println("in2 = " + y)
            println("error!! output is : "+ peek(dut.io.Sum).toString + "the answer should be : "+ (x+y))
        }
        step(1)
    }
    
    if(pass == 0){
        println("MixAdder test completed!!!!!")
    }else{
        println(s"MixAdder test failed...you have ${pass} errors to fix")
    }
    
}

object MixAdderTest extends App{
    Driver.execute(args,()=>new MixAdder(8)){
        c => new MixAdderTest(c)
    }
}