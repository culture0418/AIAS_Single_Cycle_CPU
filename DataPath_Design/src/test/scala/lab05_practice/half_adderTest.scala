// in ./src/test/scala/lab05_practice/half_adderTest.scala

package lab05_practice // todo : 盡量和 Module 相同 package (可以省去編寫路徑的麻煩)

import chisel3.iotesters.{PeekPokeTester, Driver}

class half_adderTest(c: half_adder) extends PeekPokeTester(c){
    val i0 = 0
    val i1 = 1

    poke(c.io.A, i0)
    poke(c.io.B, i1)

    // TODO: step(n) 對 combinational circuit可有可無

    // todo : test method 1
    // todo : 格式  expect(port, value)

    // io.A = 0, io.B = 1
    // io.Sum = 1, io.Carry = 0
    expect(c.io.Sum, i1)
    expect(c.io.Carry, i0)
    
}