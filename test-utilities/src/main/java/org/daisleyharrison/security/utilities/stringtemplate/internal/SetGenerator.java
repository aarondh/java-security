package org.daisleyharrison.security.utilities.stringtemplate.internal;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.daisleyharrison.security.utilities.RandomHelper;

public class SetGenerator extends GeneratorBase {
    private String name;
    private Object[] set;

    public SetGenerator(String name, Object[] set) {
        this.name = name;
        this.set = set;
    }
    @Override
    public Object generateOne() {
        return set[RandomHelper.nextInt(set.length)];
    }

    @Override
    public String toString(){
        if(this.name != null){
            return "<" + this.name + ">" + super.toString();
        } else if (set == null) {
            return "<null>" + super.toString();
        } else {
            int len = set.length;
            if( len > 10){
                return "{" + Arrays.stream(set).limit(5).map(Object::toString).collect(Collectors.joining(" ")) + " ... " + set[set.length-1] + "}" + super.toString();
            } else {
                return "{" + "{" + Arrays.stream(set).map(Object::toString).collect(Collectors.joining(" ")) + "}" + super.toString();
            }
        }
    }

}