package aias_lab5.Hw4

import chisel3._
import chisel3.util._ 


object ALU_funct3{
  val ADD_SUB_LB_SB_BEQ = "b000".U  // add sub funct3 = 000
  val SLL_LH_SH_BNE = "b001".U  // sll funct3 = 001  
  val SLT_LW_SW = "b010".U  // slt funct3 = 010
  val SLTU = "b011".U  // sltu funct3 = 011
  val XOR_LBU_BLT = "b100".U  // xor funct3 = 100
  val SRL_SRA_LHU_BGE = "b101".U  // srl 跟 sra funct3 = 101
  val OR_BLTU  = "b110".U  // or funct3 = 110 
  val AND_BGEU = "b111".U  // and funct3 = 111
}

object ALU_funct7{
  val SUB_SRA_SRAI = "b0100000".U  // sub sra srai funct7 = 0100000
  val SRL = "b0000000".U  // srL funct7 = 0000000
}

import ALU_funct3._,ALU_funct7._,opcode_map._
// input output
class ALUIO extends Bundle{
  val src1    = Input(UInt(32.W))  // src1 = Reg[rs1]
  val src2    = Input(UInt(32.W))  // src2 = Reg[rs2]
  val funct7  = Input(UInt(7.W))
  val funct3  = Input(UInt(3.W))
  val opcode  = Input(UInt(7.W))
  val out  = Output(UInt(32.W)) // ALU result
}

class ALU extends Module{
  val io = IO(new ALUIO) 
  
  io.out := MuxLookup(io.opcode, false.B, Seq(
      // I-Format
      OP_IMM -> (MuxLookup(io.funct3,false.B,
        Seq(
            // addi
            ADD_SUB_LB_SB_BEQ -> (io.src1+io.src2) , // add x27, x26, x25 // x27 = x26 + x25
            // slli
            SLL_LH_SH_BNE ->  (io.src1 << io.src2(4,0)),// sll rd, rs1, rs2 // rs2暫存器的最低 5-bit為 shift amount
            // slti
            SLT_LW_SW -> (Mux(((io.src1).asSInt < (io.src2).asSInt), true.B, false.B)), // slt rd, rs1, rs2, // rs1 < rs2 (sign), rd = 1 else rd = 0
            // sltui
            SLTU -> (Mux(((io.src1).asUInt < (io.src2).asUInt), true.B, false.B)), // slt rd, rs1, rs2, // rs1 < rs2 (unsign), rd = 1 else rd = 0
            // xori
            XOR_LBU_BLT -> (io.src1 ^ io.src2), // xor rd, rs1, rs2 // rd = rs1 ^ rs2
            // ori
            OR_BLTU -> (io.src1 | io.src2), // or rd, rs1, rs2 
            // andi
            AND_BGEU -> (io.src1 & io.src2), // and rd, rs1. rs2
            // srli srai
            SRL_SRA_LHU_BGE -> (Mux((io.funct7 === SUB_SRA_SRAI), 
                            (((io.src1).asSInt >> Cat(io.src2(4,0))).asUInt),  // sra rd, rs1, rs2 // rs2暫存器的最低 5-bit為 shift amount (arthimetic)
                            (io.src1 >> io.src2(4,0)))) // srl rd, rs1, rs2 // rs2暫存器的最低 5-bit為 shift amount (logical) 
          )
        )
      ),
      // R-Format
      OP -> (MuxLookup(io.funct3, false.B,
        Seq(
            // add sub
            ADD_SUB_LB_SB_BEQ -> (Mux((io.funct7 === SUB_SRA_SRAI), (io.src1-io.src2), (io.src1+io.src2))) , // add x27, x26, x25 // x27 = x26 + x25
            // sll
            SLL_LH_SH_BNE ->  (io.src1 << (io.src2(4,0).asUInt)),// sll rd, rs1, rs2 // rs2暫存器的最低 5-bit為 shift amount
            // slt
            SLT_LW_SW -> (Mux(((io.src1).asSInt < (io.src2).asSInt), 1.U, 0.U)), // slt rd, rs1, rs2, // rs1 < rs2 (sign), rd = 1 else rd = 0
            // sltu
            SLTU -> (Mux(((io.src1).asUInt < (io.src2).asUInt), 1.U, 0.U)), // slt rd, rs1, rs2, // rs1 < rs2 (unsign), rd = 1 else rd = 0
            // xor
            XOR_LBU_BLT -> (io.src1 ^ io.src2), // xor rd, rs1, rs2 // rd = rs1 ^ rs2
            // or 
            OR_BLTU -> (io.src1 | io.src2), // or rd, rs1, rs2 
            // and
            AND_BGEU -> (io.src1 & io.src2), // and rd, rs1. rs2
            // srl sra
            SRL_SRA_LHU_BGE -> (Mux((io.funct7 === SUB_SRA_SRAI), 
                            (((io.src1).asSInt >> Cat(io.src2(4,0))).asUInt),  // sra rd, rs1, rs2 // rs2暫存器的最低 5-bit為 shift amount (arthimetic)
                            (io.src1 >> io.src2(4,0)))) // srl rd, rs1, rs2 // rs2暫存器的最低 5-bit為 shift amount (logical)
          )
        )
      ),
      // LOAD
      LOAD ->(MuxLookup(io.funct3, false.B, 
        Seq(
          // lb 
          ADD_SUB_LB_SB_BEQ -> (io.src1 + io.src2),
          // lh
          SLL_LH_SH_BNE -> (io.src1 + io.src2),
          // lw
          SLT_LW_SW -> (io.src1 + io.src2),
          // lbu
          XOR_LBU_BLT -> (io.src1 + io.src2),
          // lhu
          SRL_SRA_LHU_BGE -> (io.src1 + io.src2),
          )      
        )
      ),
      // store
      STORE -> (MuxLookup(io.funct3, false.B, 
        Seq(
          // sb 
          ADD_SUB_LB_SB_BEQ -> (io.src1 + io.src2),
          // sh
          SLL_LH_SH_BNE -> (io.src1 + io.src2),
          // sw
          SLT_LW_SW -> (io.src1 + io.src2),
          )      
        )
      ),
      // branch
      BRANCH -> (MuxLookup(io.funct3, false.B,
        Seq(
            // beq rs1 rs2 simm13 將 signed 13-bit的最高 12位元放入指令編碼中
            ADD_SUB_LB_SB_BEQ -> (io.src1+io.src2), 
            // bne
            SLL_LH_SH_BNE ->  (io.src1+io.src2),
            // blt
            XOR_LBU_BLT -> (io.src1+io.src2), 
            // bltu 
            OR_BLTU -> (io.src1+io.src2), 
            // bgeu
            AND_BGEU -> (io.src1+io.src2), 
            // bge
            SRL_SRA_LHU_BGE -> (io.src1+io.src2), 
          )
        )
      ),
      // jal
      JAL -> (io.src1 + io.src2),
      
      // jalr
      JALR -> (io.src1 + io.src2),
    )
  )
}