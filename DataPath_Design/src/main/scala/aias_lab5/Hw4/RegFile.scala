package aias_lab5.Hw4
import chisel3._

// TODO:記得在top宣告時要傳入參數 readPorts !!!

class RegFile(readPorts:Int) extends Module {
  val io = IO(new Bundle{
   val wen = Input(Bool())  // write enable
   val waddr = Input(UInt(5.W)) // write address
   val wdata = Input(UInt(32.W)) // write data
   val raddr = Input(Vec(readPorts, UInt(5.W))) // read address
   val rdata = Output(Vec(readPorts, UInt(32.W))) // read data
 })

  // 1. the reset value of all regs is zero
  // val regs = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  // 2. the reset value of all regs is their index
  val regs = RegInit(VecInit(Seq.range(0,32).map{x=>x.U(32.W)}))
  
  //Wiring
  (io.rdata zip io.raddr).map{case(data,addr)=>data:=regs(addr)} // read data
  when(io.wen) {regs(io.waddr) := io.wdata} // write back
  regs(0) := 0.U // x0 = 0
  
  // for add test
  // regs(9) := 9.U
  // regs(11) := 11.U

}