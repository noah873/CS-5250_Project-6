import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Assembler {
    private static int currentVariableAddress = 16;
    private static final Map<String, Integer> symbolTable = new HashMap<>();
    private static final Map<String, String> compTable = new HashMap<>();
    private static final Map<String, String> destTable = new HashMap<>();
    private static final Map<String, String> jumpTable = new HashMap<>();

    static {
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);
        symbolTable.put("R0", 0);
        symbolTable.put("R1", 1);
        symbolTable.put("R2", 2);
        symbolTable.put("R3", 3);
        symbolTable.put("R4", 4);
        symbolTable.put("R5", 5);
        symbolTable.put("R6", 6);
        symbolTable.put("R7", 7);
        symbolTable.put("R8", 8);
        symbolTable.put("R9", 9);
        symbolTable.put("R10", 10);
        symbolTable.put("R11", 11);
        symbolTable.put("R12", 12);
        symbolTable.put("R13", 13);
        symbolTable.put("R14", 14);
        symbolTable.put("R15", 15);
        compTable.put("0", "0101010");
        compTable.put("1", "0111111");
        compTable.put("-1", "0111010");
        compTable.put("D", "0001100");
        compTable.put("A", "0110000");
        compTable.put("!D", "0001101");
        compTable.put("!A", "0110001");
        compTable.put("-D", "0001111");
        compTable.put("-A", "0110011");
        compTable.put("D+1", "0011111");
        compTable.put("A+1", "0110111");
        compTable.put("D-1", "0001110");
        compTable.put("A-1", "0110010");
        compTable.put("D+A", "0000010");
        compTable.put("D-A", "0010011");
        compTable.put("A-D", "0000111");
        compTable.put("D&A", "0000000");
        compTable.put("D|A", "0010101");
        compTable.put("M", "1110000");
        compTable.put("!M", "1110001");
        compTable.put("-M", "1110011");
        compTable.put("M+1", "1110111");
        compTable.put("M-1", "1110010");
        compTable.put("D+M", "1000010");
        compTable.put("D-M", "1010011");
        compTable.put("M-D", "1000111");
        compTable.put("D&M", "1000000");
        compTable.put("D|M", "1010101");
        destTable.put("", "000");
        destTable.put("M", "001");
        destTable.put("D", "010");
        destTable.put("MD", "011");
        destTable.put("A", "100");
        destTable.put("AM", "101");
        destTable.put("AD", "110");
        destTable.put("AMD", "111");
        jumpTable.put("", "000");
        jumpTable.put("JGT", "001");
        jumpTable.put("JEQ", "010");
        jumpTable.put("JGE", "011");
        jumpTable.put("JLT", "100");
        jumpTable.put("JNE", "101");
        jumpTable.put("JLE", "110");
        jumpTable.put("JMP", "111");
    }

    private static void assemble(String asmPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(asmPath));

        int ROM = 0;
        for (String line : lines) {
            line = line.replaceAll("//.*", "").trim();

            if (!line.isEmpty()) {
                if (line.startsWith("(")) {
                    String label = line.substring(1, line.length() - 1);
                    symbolTable.put(label, ROM);

                } else {
                    ROM++;
                }
            }
        }

        List<String> binaryLines = new ArrayList<>();

        for (String line : lines) {
            line = line.replaceAll("//.*", "").trim();

            if (!line.isEmpty() && !line.startsWith("(")) {
                if (line.startsWith("@")) {
                    String symbol = line.substring(1);
                    int address;

                    if (Character.isDigit(symbol.charAt(0))) {
                        address = Integer.parseInt(symbol);

                    } else {
                        address = symbolTable.computeIfAbsent(symbol, k -> currentVariableAddress++);
                    }

                    binaryLines.add(String.format("%16s", Integer.toBinaryString(address)).replace(' ', '0'));

                } else {
                    String dest = "", comp = line, jump = "";

                    if (line.contains("=")) {
                        String[] parts = line.split("=");
                        dest = parts[0];
                        comp = parts[1];
                    } if (comp.contains(";")) {
                        String[] parts = comp.split(";");
                        comp = parts[0];
                        jump = parts[1];
                    }

                    String compBits = compTable.get(comp.trim());
                    String destBits = destTable.get(dest.trim());
                    String jumpBits = jumpTable.get(jump.trim());
                    binaryLines.add("111" + compBits + destBits + jumpBits);
                }
            }
        }
        List<String> binary =  binaryLines;

        String outPath = asmPath.replace(".asm", ".hack");

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outPath))) {
            for (String binaryLine : binary) {
                writer.write(binaryLine);
                writer.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            assemble(args[0]);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}