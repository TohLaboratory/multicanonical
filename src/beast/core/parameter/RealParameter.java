package beast.core.parameter;

import beast.core.Description;
import beast.core.Input;
import beast.core.State;

import java.io.PrintStream;

/**
 * @author Alexei Drummond
 */

@Description("A real-valued parameter represents a value in the state space that can be changed " +
        "by operators.")
public class RealParameter extends Parameter<Double> {
    public Input<Double> m_pValues = new Input<Double>("value", "start value for this parameter");
    public Input<Double> lowerValueInput = new Input<Double>("lower", "lower value for this parameter");
    public Input<Double> upperValueInput = new Input<Double>("upper", "upper value for this parameter");

    public Double getValue() {
        return values[0];
    }

    @Override
    public void initAndValidate(State state) throws Exception {
        m_fLower = lowerValueInput.get();
        m_fUpper = upperValueInput.get();

        System.out.println("Lower = " + m_fLower);
        System.out.println("Upper = " + m_fUpper);

        values = new java.lang.Double[m_nDimension.get()];
        for (int i = 0; i < values.length; i++) {
            values[i] = m_pValues.get();
        }
    }

    public void setValue(Double fValue) throws Exception {

        if (fValue < getLower() || fValue > getUpper()) throw new IllegalArgumentException("new value outside bounds!");

        if (isStochastic()) {
            values[0] = fValue;
            setDirty(true);
        } else throw new Exception("Can't set the value of a fixed parameter.");
    }


    /**
     * deep copy *
     */
    public Parameter<?> copy() {
        RealParameter copy = new RealParameter();
        copy.setID(getID());
        copy.values = new java.lang.Double[values.length];
        System.arraycopy(values, 0, copy.values, 0, values.length);
        copy.m_fLower = m_fLower;
        copy.m_fUpper = m_fUpper;
        return copy;
    }

    public void log(int nSample, State state, PrintStream out) {
        RealParameter var = (RealParameter) state.getStateNode(m_sID);
        int nValues = var.getDimension();
        for (int iValue = 0; iValue < nValues; iValue++) {
            out.print(var.getValue(iValue) + "\t");
        }
    }
}


