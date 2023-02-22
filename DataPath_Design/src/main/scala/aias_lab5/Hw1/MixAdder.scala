package aias_lab5.Hw1

import chisel3._
import chisel3.util._
import aias_lab5.Lab._

class MixAdder (n:Int) extends Module{
  val io = IO(new Bundle{
      val Cin = Input(UInt(1.W))
      val in1 = Input(UInt((4*n).W))
      val in2 = Input(UInt((4*n).W))
      val Sum = Output(UInt((4*n).W))
      val Cout = Output(UInt(1.W))
  })
  // please implement your code below
  // CLAdder ports: in1, in2, Cin, Sum, Cout
  val CLAdder_Array = Array.fill(8)(Module(new CLAdder()).io)
  val carry = Wire(Vec(9, UInt(1.W)))
  val sum = Wire(Vec(8,UInt(4.W)))
  val four_bit_in1 = Wire(Vec(8, UInt(4.W)))
  val four_bit_in2 = Wire(Vec(8, UInt(4.W)))

  carry(0) := io.Cin
  // input 32 bit divided to 4_bit 8 group

  for (i <- 0 until 8){
    four_bit_in1(i) := Cat(io.in1(4*i+3), io.in1(4*i+2), io.in1(4*i+1), io.in1(4*i))
    four_bit_in2(i) := Cat(io.in2(4*i+3), io.in2(4*i+2), io.in2(4*i+1), io.in2(4*i))
  }
  // 4_bit_in1(0) = Cat(io.in1(0), io.in1(1), io.in1(2), io.in1(3))
  // 4_bit_in1(1) = Cat(io.in1(4), io.in1(5), io.in1(6), io.in1(7))
  // 4_bit_in1(2) = Cat(io.in1(8), io.in1(9), io.in1(10), io.in1(11))
  // 4_bit_in1(3) = Cat(io.in1(12), io.in1(13), io.in1(14), io.in1(15))

  for (i <- 0 until 8){
    CLAdder_Array(i).in1 := four_bit_in1(i)
    CLAdder_Array(i).in2 := four_bit_in2(i)
    CLAdder_Array(i).Cin := carry(i)
    carry(i+1) := CLAdder_Array(i).Cout
    sum(i) := CLAdder_Array(i).Sum
  }

  io.Sum := sum.asUInt
  io.Cout := CLAdder_Array(7).Cout
}