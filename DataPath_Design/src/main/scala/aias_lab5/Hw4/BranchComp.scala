package aias_lab5.Hw4

import chisel3._
import chisel3.util._

object condition{
  val EQ = "b000".U  //  beq funct3 000
  val NE = "b001".U  //  bne funct3 001
  val LT = "b100".U  //  blt funct3 100
  val GE = "b101".U  //  bge funct3 101
  val LTU = "b110".U  //  bltu funct3 110
  val GEU = "b111".U  //  bgeu funct3 111
}

import condition._

class BranchComp extends Module{
    val io = IO(new Bundle{
        val en = Input(Bool())
        val funct3 = Input(UInt(3.W))
        val src1 = Input(UInt(32.W))
        val src2 = Input(UInt(32.W))

        val brtaken = Output(Bool()) //for pc.io.brtaken
    })
    
    //please implement your code below
    
    // initialize
    io.brtaken := false.B

    when(io.en === true.B){
      io.brtaken := MuxLookup(io.funct3,false.B,
        Seq(
          EQ -> Mux((io.src1 === io.src2), true.B, false.B), // beq rs1, rs2, simm13
          NE -> Mux((io.src1 =/= io.src2), true.B, false.B), // bne rs1, rs2, simm13
          LT -> Mux((io.src1.asSInt < io.src2.asSInt), true.B, false.B), // blt rs1, rs2 (sign compare)
          GE -> Mux((io.src1.asSInt >= io.src2.asSInt), true.B, false.B), // bge rs1, rs2 (sign compare)
          LTU -> Mux((io.src1.asUInt < io.src2.asUInt), true.B, false.B), // bltu rs1, rs2 (unsign compare)
          GEU -> Mux((io.src1.asUInt >= io.src2.asUInt), true.B, false.B), // bgeu rs1, rs2 (unsign compare)
          ) 
        )
    } 
}