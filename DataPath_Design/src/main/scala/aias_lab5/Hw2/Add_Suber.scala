// C:\Users\USER\Documents\aias\repo\playlab-docker-base\projects\Lab05\src\main\scala\aias_lab5\Hw2\Add_Suber.scala
package aias_lab5.Hw2

import chisel3._
import chisel3.util._
import aias_lab5.Lab._

class Add_Suber extends Module{
  val io = IO(new Bundle{
  val in_1 = Input(UInt(4.W))
	val in_2 = Input(UInt(4.W))
	val op = Input(Bool()) // 0:ADD 1:SUB
	val out = Output(UInt(4.W))
	val o_f = Output(Bool())
  })

  //please implement your code below

  // todo : op：operation，決定該做加法(0)還是減法(1)

  // Module Declaration
  // FullAdder : A B Cin Sum Cout
  val FA_Array = Array.fill(4)(Module(new FullAdder()).io)
  val NB = Wire(Vec(4,UInt(1.W))) // 放io.in_2 ^ io.op result
  val sum = Wire(Vec(4,Bool()))
  val carry = Wire(Vec(5,UInt(1.W)))
  
  carry(0) := io.op

  // Wiring
  for (i <- 0 until 4){
    NB(i) := io.in_2(i) ^ io.op // NB = io.in_2 XOR io.op
    FA_Array(i).A := io.in_1(i)
    FA_Array(i).B := NB(i)
    FA_Array(i).Cin := carry(i)
    carry(i+1) := FA_Array(i).Cout
    sum(i) := FA_Array(i).Sum
  }
  
  io.out := sum.asUInt
  // printf("sum = %x\n", sum.asUInt)
  io.o_f := carry(3)^carry(4)
}
