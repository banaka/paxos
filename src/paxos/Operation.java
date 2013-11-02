package paxos;

public class Operation {
    OpertationType opType;
    String op;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Operation operation = (Operation) o;

        if (!op.equals(operation.op)) return false;
        if (opType != operation.opType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = opType.hashCode();
        result = 31 * result + op.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "opType=" + opType +

                ", op='" + op + '\'' +
                '}';
    }

    Operation(String opType, String op) {
        this.op = op;
        this.opType = OpertationType.valueOf(opType);
    }



    public enum OpertationType {
        Deposit("Deposit"),
        Withdraw("Withdraw"),
        Transfer("Transfer"),
        Inquiry("Inquiry");

        public String opType;

        OpertationType(String opType) {
            this.opType = opType;
        }
    }
}
