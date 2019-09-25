package org.daisleyharrison.security.utilities.stringtemplate.internal;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.daisleyharrison.security.utilities.RandomHelper;
import org.daisleyharrison.security.utilities.stringtemplate.Generator;
import org.daisleyharrison.security.utilities.stringtemplate.Modifier;

public abstract class GeneratorBase implements Generator {
    private double probability;
    private double min;
    private double max;
    private String separator;
    private Modifier modifier;

    public GeneratorBase() {
        probability = 1.0;
        min = 1.0;
        max = 1.0;
        separator = "";
        modifier = t -> t;
    }

    /**
     * @return double return the probability
     */
    public double getProbability() {
        return probability;
    }

    /**
     * @param probability the probability to set
     */
    public void setProbability(double probability) {
        this.probability = probability;
    }

    public boolean shouldGenerate() {
        return RandomHelper.nextDouble() <= probability;
    }

    /**
     * @return double return the min
     */
    public double getMin() {
        return min;
    }

    /**
     * @param min the min to set
     */
    public void setMin(double min) {
        this.min = min;
    }

    /**
     * @return double return the max
     */
    public double getMax() {
        return max;
    }

    /**
     * @param max the max to set
     */
    public void setMax(double max) {
        this.max = max;
    }


    /**
     * @return String return the separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @param separator the separator to set
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /**
     * @return Modifier return the modifier
     */
    public Modifier getModifier() {
        return modifier;
    }

    /**
     * @param modifier the modifier to set
     */
    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public abstract Object generateOne();

    @Override
    public Object generate() {
        if (shouldGenerate()) {
            List<Object> results = new LinkedList<>();
            int numberToGenerate;
            double min = getMin();
            double max = getMax();
            if (min < max) {
                numberToGenerate = RandomHelper.nextInt((int) min, (int) max);
            } else {
                numberToGenerate = (int) max;
            }
            if (numberToGenerate <= 0) {
                return "";
            }
            while (numberToGenerate-- > 0) {
                Object item = getModifier().apply(generateOne());
                if(item!=null){
                    results.add(item);
                }
            }
            return results.stream().map(Object::toString).collect(Collectors.joining(getSeparator()));
        }
        return "";
    }

    @Override
    public String toString() {
        return String.format("|%s|[%.2f %.0f-%.0f]", getSeparator(), getProbability(), getMin(), getMax());
    }

}