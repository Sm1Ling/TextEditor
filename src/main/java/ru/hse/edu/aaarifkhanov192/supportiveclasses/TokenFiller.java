package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import org.antlr.v4.runtime.Token;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;

import java.util.HashMap;
import java.util.List;

public class TokenFiller {

    //region HashMap of tokens
    static final HashMap<Integer, String> tokensMap = new HashMap<>(128);
    static {
        tokensMap.put(79,"number");
        tokensMap.put(80,"number");
        tokensMap.put(29,"program");
        tokensMap.put(3,"begin");
        tokensMap.put(38,"var");
        tokensMap.put(78,"string");
        //tokensMap.put(77,"ident"); их пока не добавляю
        tokensMap.put(1,"keyword");
        tokensMap.put(2,"keyword");
        tokensMap.put(4,"keyword");
        tokensMap.put(5,"keyword");
        tokensMap.put(6,"keyword");
        tokensMap.put(7,"keyword");
        tokensMap.put(8,"keyword");
        tokensMap.put(9,"keyword");
        tokensMap.put(10,"keyword");
        tokensMap.put(11,"keyword");
        tokensMap.put(12,"keyword");
        tokensMap.put(13,"keyword");
        tokensMap.put(14,"keyword");
        tokensMap.put(15,"keyword");
        tokensMap.put(16,"keyword");
        tokensMap.put(17,"keyword");
        tokensMap.put(18,"keyword");
        tokensMap.put(19,"keyword");
        tokensMap.put(20,"keyword");
        tokensMap.put(21,"keyword");
        tokensMap.put(22,"keyword");
        tokensMap.put(23,"keyword");
        tokensMap.put(24,"keyword");
        tokensMap.put(25,"keyword");
        tokensMap.put(26,"keyword");
        tokensMap.put(28,"keyword");
        tokensMap.put(30,"keyword");
        tokensMap.put(31,"keyword");
        tokensMap.put(32,"keyword");
        tokensMap.put(33,"keyword");
        tokensMap.put(34,"keyword");
        tokensMap.put(35,"keyword");
        tokensMap.put(36,"keyword");
        tokensMap.put(37,"keyword");
        tokensMap.put(39,"keyword");
        tokensMap.put(40,"keyword");
        tokensMap.put(67,"keyword");
        tokensMap.put(68,"keyword");
        tokensMap.put(69,"keyword");
        tokensMap.put(70,"keyword");
        tokensMap.put(71,"keyword");
        tokensMap.put(72,"keyword");
        tokensMap.put(73,"keyword");
        tokensMap.put(41,"operation");
        tokensMap.put(42,"operation");
        tokensMap.put(43,"operation");
        tokensMap.put(44,"operation");
        tokensMap.put(45,"operation");
        tokensMap.put(49,"operation");
        tokensMap.put(50,"operation");
        tokensMap.put(51,"operation");
        tokensMap.put(52,"operation");
        tokensMap.put(53,"operation");
        tokensMap.put(54,"operation");
        tokensMap.put(61,"operation");
        tokensMap.put(62,"operation");
        tokensMap.put(46,"comma");
        tokensMap.put(47,"comma");
        tokensMap.put(48,"comma");
        tokensMap.put(63,"comma");
        tokensMap.put(64,"comma");
        tokensMap.put(55,"parentheses");
        tokensMap.put(56,"parentheses");
        tokensMap.put(57,"parentheses");
        tokensMap.put(58,"parentheses");
        tokensMap.put(59,"parentheses");
        tokensMap.put(60,"parentheses");
        tokensMap.put(65,"parentheses");
        tokensMap.put(66,"parentheses");
        tokensMap.put(75,"comment");
        tokensMap.put(76,"comment");
    }
    //endregion

    public static void refillColorTree(List<? extends Token> tokens, MyIntervalTree<String> tree){
        for(var token : tokens){
            if(tokensMap.containsKey(token.getType())) {
                tree.insert(token.getStartIndex(), token.getStopIndex(), tokensMap.get(token.getType()));
            }
        }
    }
}
