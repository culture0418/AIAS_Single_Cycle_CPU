package aias_lab5.Hw4

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

// 分辨不同資料長度
object wide {
  val Byte = "b000".U // lb funct3 = 000
  val Half = "b001".U // lh funct 3 = 001
  val Word = "b010".U // lw funct3 = 010
  val UByte = "b100".U // lbu funct3 = 100
  val UHalf = "b101".U // lhu funct3 = 101
}

import wide._

class DataMem extends Module {
  val io = IO(new Bundle {
    val funct3 = Input(UInt(32.W))
    val raddr = Input(UInt(10.W))
    val rdata = Output(SInt(32.W))
    
    val wen   = Input(Bool())
    val waddr = Input(UInt(10.W))
    val wdata = Input(UInt(32.W))
  })

  val memory = Mem(32, UInt(8.W))
  loadMemoryFromFile(memory, "./src/main/resource/DataMem.txt")

  io.rdata := 0.S

  val wa = WireDefault(0.U(10.W)) // address
  val wd = WireDefault(0.U(32.W)) // data

  wa := MuxLookup(io.funct3,0.U(10.W),Seq(
    Byte -> io.waddr,
    Half -> io.waddr, 
    Word -> io.waddr, 
  ))

  wd := MuxLookup(io.funct3,0.U,Seq(
    Byte -> io.wdata, 
    Half -> io.wdata, 
    Word -> io.wdata,
  ))

  when(io.wen){ //STORE
    when(io.funct3===Byte){ //  M[rs1+imm](7,0) = rs2(7,0)
      memory(wa) := wd(7,0)
    }.elsewhen(io.funct3===Half){ //  M[rs1+imm](15,0) = rs2(15,0)
      memory(wa) := wd(7,0) // 2 個 8_bit 逐一放進去
      memory(wa+1.U) := wd(15,8) 
    }.elsewhen(io.funct3===Word){ //  M[rs1+imm](31,0) = rs2(31,0)
      for (i <- 0 until 4){
        memory(wa+(i).asUInt) := wd((8*i+7),(8*i)) // 4 個 8_bit 逐一放進去
        // memory(wa) := wd(7,0)
        // memory(wa+1.U) := wd(15,8)
      }
    }
  }.otherwise{ //LOAD 
    io.rdata := MuxLookup(io.funct3,0.S,Seq( // io.rdata 32bit
      Byte -> memory(io.raddr)(7,0).asSInt,
      Half -> Cat(memory(io.raddr+1.U)(7,0), memory(io.raddr)(7,0)).asSInt, //  2 個 8_bit
      Word ->  Cat(memory(io.raddr+3.U)(7,0), memory(io.raddr+2.U)(7,0), memory(io.raddr+1.U)(7,0), memory(io.raddr)(7,0)).asSInt, 
      UByte -> Cat(0.U(24.W), memory(io.raddr)(7,0)).asSInt, // unsign 前面補0
      UHalf -> Cat(0.U(16.W), memory(io.raddr+1.U)(7,0), memory(io.raddr)(7,0)).asSInt // unsign 前面補0
    ))
  }
}