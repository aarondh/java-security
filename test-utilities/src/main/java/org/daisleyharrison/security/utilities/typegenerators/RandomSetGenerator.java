package org.daisleyharrison.security.utilities.typegenerators;

import org.daisleyharrison.security.utilities.TypeGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.daisleyharrison.security.utilities.RandomHelper;

public class RandomSetGenerator<T> implements TypeGenerator<T> {

    private String pattern;
    private List<T> set = Collections.emptyList();

    public RandomSetGenerator(String pattern, Set<T> set) {
        this.pattern = pattern;
        if(set!=null){
            this.set = new ArrayList<>(set);
        }
    }

    public RandomSetGenerator(String pattern, Collection<T> set) {
        this.pattern = pattern;
        if(set!=null){
            this.set = new ArrayList<>(set);
        }
    }

    public RandomSetGenerator(String pattern, T[] set) {
        this.pattern = pattern;
        if(set!=null){
            this.set = Arrays.asList(set);
        }
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public T apply(Class<T> type, String name) {
        return set.get(RandomHelper.nextInt(set.size()));
    }

}
