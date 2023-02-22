package aias_lab5.Hw3

import chisel3._
import aias_lab5._
import aias_lab5.Hw1._
import chisel3.util._
import scala.annotation.switch

//------------------Radix 4---------------------
class Booth_Mul(width:Int) extends Module {
  val io = IO(new Bundle{ 
    val in1 = Input(UInt(width.W))      //Multiplicand in1:A
    val in2 = Input(UInt(width.W))      //Multiplier in2:B
    val out = Output(UInt((2*width).W)) //product
  })
  //please implement your code below
  val Booth_Mul_Array = Array.fill(width/2)(Module(new Booth_Encoder(width)).io) // 每次看3bit，總共會有8組

  // Booth_encoder input: in1_e_A (in1:A), in2_e_B(in2:B), 
  //               output:Partial_Product

  val en_long_in2 = (Cat(io.in2, 0.U(1.W))).asUInt// tmp_in2 放加上 overlap 的 0 (16+1) bit
  val en_PP = Wire(Vec(width/2, SInt((width*2).W)))
  val sing_in1 = Wire(Vec(width/2, SInt((width*2).W)))

  for (i <- 0 until width/2){
    sing_in1(i) := io.in1.asSInt
    Booth_Mul_Array(i).in1_e_A := sing_in1(i).asUInt
    Booth_Mul_Array(i).in2_e_B := en_long_in2(2*(i+1), 2*i)
    en_PP(i) := (Booth_Mul_Array(i).Partial_Product) << (2*i)
    
  }
  // Booth_Mul_Array(0).in1_e_A := io.in1
  // Booth_Mul_Array(0).in2_e_B := en_long_in2(2,0)
  
  // Booth_Mul_Array(7).in1_e_A := io.in1
  // Booth_Mul_Array(7).in2_e_B := en_long_in2(16,14)

  // adder
  // 16 bit 3 adder_level
  // 32 bit 4 adder_level
  val adder_Sum_Array = Wire(Vec((width/2 - 1), SInt((2*width).W))) // 8 partial product 兩兩相加 需要7個adder
  var adder_level = (width)

  for(i <- 0 until (width/4)){
    adder_Sum_Array(i) := en_PP(2*i) + en_PP(2*i+1)
  }
  // first adder_level: 4 adders
  // adder_Sum_Array(0) := en_PP(0) + en_PP(1)
  // adder_Sum_Array(1) := en_PP(2) + en_PP(3)
  // adder_Sum_Array(2) := en_PP(4) + en_PP(5)
  // adder_Sum_Array(3) := en_PP(6) + en_PP(7)
  
  // second adder_level: 2 adders
  adder_Sum_Array(4) := adder_Sum_Array(0) + adder_Sum_Array(1)
  adder_Sum_Array(5) := adder_Sum_Array(2) + adder_Sum_Array(3)

  // third adder_level: 1 adder
  adder_Sum_Array(6) := adder_Sum_Array(4) + adder_Sum_Array(5)

  //operation
  io.out := adder_Sum_Array(6).asUInt
  
  

  
  
  
}

