package com.compiler;

import java.util.ArrayList;

public class Item implements Comparable<Item>{
	public String variable;
	public ArrayList<String> state;
	public String expecSymbol;
	public int position;
	
	public Item(String variable, ArrayList<String> state, String expecSymbol) {
		this.variable = variable;
		this.expecSymbol = expecSymbol;
		this.state = state;
		for(int i = 0; i < state.size();i++) {
			if(state.get(i).equals(".")) {
				this.position = i;
				break;
			}
		}
	}
	
	@Override
	public boolean equals(Object object) {
        if (object instanceof Item) {
        	Item otherItem = (Item) object;
            if (this.variable.equals(otherItem.variable) && this.expecSymbol.equals(otherItem.expecSymbol) && this.state.size() == otherItem.state.size()) {
            	for(int i = 0; i < this.state.size(); i++) {
            		if(!this.state.get(i).equals(otherItem.state.get(i))) {
            			return false;
            		}
            	}
                return true;
            }
        }
        return false;
    }

	@Override
	public int compareTo(Item o) {
		String selfStr = this.variable;
		for(String str : state) {
			selfStr = selfStr + str;
		}
		selfStr = selfStr + this.expecSymbol;
		String oStr = o.variable;
		for(String str : o.state) {
			oStr = oStr + str;
		}
		oStr = oStr + o.expecSymbol;
		return selfStr.compareTo(oStr);
	}

	
}
