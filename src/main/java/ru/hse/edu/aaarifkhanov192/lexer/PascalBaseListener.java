// Generated from D:/Study/Sharajnoye/���������� IDE/gitHub/TextEditor/src/main/antlr4\Pascal.g4 by ANTLR 4.9.1

package ru.hse.edu.aaarifkhanov192.lexer;


import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.TextCanvas;
import ru.hse.edu.aaarifkhanov192.supportiveclasses.intervaltree.MyIntervalTree;


public class PascalBaseListener implements PascalListener {

	MyIntervalTree<String> highlight;
	public PascalBaseListener(MyIntervalTree<String> highlight){
		this.highlight = highlight;
	}

	@Override public void enterProgram(PascalParser.ProgramContext ctx) { }

	@Override public void exitProgram(PascalParser.ProgramContext ctx) { }

	@Override public void enterProgramHeading(PascalParser.ProgramHeadingContext ctx) { }

	@Override public void exitProgramHeading(PascalParser.ProgramHeadingContext ctx) { }

	@Override public void enterIdentifier(PascalParser.IdentifierContext ctx) { }

	@Override public void exitIdentifier(PascalParser.IdentifierContext ctx) { }

	@Override public void enterBlock(PascalParser.BlockContext ctx) { }

	@Override public void exitBlock(PascalParser.BlockContext ctx) { }

	@Override public void enterUsesUnitsPart(PascalParser.UsesUnitsPartContext ctx) { }

	@Override public void exitUsesUnitsPart(PascalParser.UsesUnitsPartContext ctx) { }

	@Override public void enterLabelDeclarationPart(PascalParser.LabelDeclarationPartContext ctx) { }

	@Override public void exitLabelDeclarationPart(PascalParser.LabelDeclarationPartContext ctx) { }

	@Override public void enterLabel(PascalParser.LabelContext ctx) { }

	@Override public void exitLabel(PascalParser.LabelContext ctx) { }

	@Override public void enterConstantDefinitionPart(PascalParser.ConstantDefinitionPartContext ctx) { }

	@Override public void exitConstantDefinitionPart(PascalParser.ConstantDefinitionPartContext ctx) { }

	@Override public void enterConstantDefinition(PascalParser.ConstantDefinitionContext ctx) { }

	@Override public void exitConstantDefinition(PascalParser.ConstantDefinitionContext ctx) { }

	@Override public void enterConstantChr(PascalParser.ConstantChrContext ctx) { }

	@Override public void exitConstantChr(PascalParser.ConstantChrContext ctx) { }

	@Override public void enterConstant(PascalParser.ConstantContext ctx) { }

	@Override public void exitConstant(PascalParser.ConstantContext ctx) { }

	@Override public void enterUnsignedNumber(PascalParser.UnsignedNumberContext ctx) { }

	@Override public void exitUnsignedNumber(PascalParser.UnsignedNumberContext ctx) { }

	@Override public void enterUnsignedInteger(PascalParser.UnsignedIntegerContext ctx) { }

	@Override public void exitUnsignedInteger(PascalParser.UnsignedIntegerContext ctx) { }

	@Override public void enterUnsignedReal(PascalParser.UnsignedRealContext ctx) { }

	@Override public void exitUnsignedReal(PascalParser.UnsignedRealContext ctx) { }

	@Override public void enterSign(PascalParser.SignContext ctx) { }

	@Override public void exitSign(PascalParser.SignContext ctx) { }

	@Override public void enterBool_(PascalParser.Bool_Context ctx) { }

	@Override public void exitBool_(PascalParser.Bool_Context ctx) { }

	@Override public void enterString(PascalParser.StringContext ctx) { }

	@Override public void exitString(PascalParser.StringContext ctx) { }

	@Override public void enterTypeDefinitionPart(PascalParser.TypeDefinitionPartContext ctx) { }

	@Override public void exitTypeDefinitionPart(PascalParser.TypeDefinitionPartContext ctx) { }

	@Override public void enterTypeDefinition(PascalParser.TypeDefinitionContext ctx) { }

	@Override public void exitTypeDefinition(PascalParser.TypeDefinitionContext ctx) { }

	@Override public void enterFunctionType(PascalParser.FunctionTypeContext ctx) { }

	@Override public void exitFunctionType(PascalParser.FunctionTypeContext ctx) { }

	@Override public void enterProcedureType(PascalParser.ProcedureTypeContext ctx) { }

	@Override public void exitProcedureType(PascalParser.ProcedureTypeContext ctx) { }

	@Override public void enterType_(PascalParser.Type_Context ctx) { }

	@Override public void exitType_(PascalParser.Type_Context ctx) { }

	@Override public void enterSimpleType(PascalParser.SimpleTypeContext ctx) { }

	@Override public void exitSimpleType(PascalParser.SimpleTypeContext ctx) { }

	@Override public void enterScalarType(PascalParser.ScalarTypeContext ctx) { }

	@Override public void exitScalarType(PascalParser.ScalarTypeContext ctx) { }

	@Override public void enterSubrangeType(PascalParser.SubrangeTypeContext ctx) { }

	@Override public void exitSubrangeType(PascalParser.SubrangeTypeContext ctx) { }

	@Override public void enterTypeIdentifier(PascalParser.TypeIdentifierContext ctx) { }

	@Override public void exitTypeIdentifier(PascalParser.TypeIdentifierContext ctx) { }

	@Override public void enterStructuredType(PascalParser.StructuredTypeContext ctx) { }

	@Override public void exitStructuredType(PascalParser.StructuredTypeContext ctx) { }

	@Override public void enterUnpackedStructuredType(PascalParser.UnpackedStructuredTypeContext ctx) { }

	@Override public void exitUnpackedStructuredType(PascalParser.UnpackedStructuredTypeContext ctx) { }

	@Override public void enterStringtype(PascalParser.StringtypeContext ctx) { }

	@Override public void exitStringtype(PascalParser.StringtypeContext ctx) { }

	@Override public void enterArrayType(PascalParser.ArrayTypeContext ctx) { }

	@Override public void exitArrayType(PascalParser.ArrayTypeContext ctx) { }

	@Override public void enterTypeList(PascalParser.TypeListContext ctx) { }

	@Override public void exitTypeList(PascalParser.TypeListContext ctx) { }

	@Override public void enterIndexType(PascalParser.IndexTypeContext ctx) { }

	@Override public void exitIndexType(PascalParser.IndexTypeContext ctx) { }

	@Override public void enterComponentType(PascalParser.ComponentTypeContext ctx) { }

	@Override public void exitComponentType(PascalParser.ComponentTypeContext ctx) { }

	@Override public void enterRecordType(PascalParser.RecordTypeContext ctx) { }

	@Override public void exitRecordType(PascalParser.RecordTypeContext ctx) { }

	@Override public void enterFieldList(PascalParser.FieldListContext ctx) { }

	@Override public void exitFieldList(PascalParser.FieldListContext ctx) { }

	@Override public void enterFixedPart(PascalParser.FixedPartContext ctx) { }

	@Override public void exitFixedPart(PascalParser.FixedPartContext ctx) { }

	@Override public void enterRecordSection(PascalParser.RecordSectionContext ctx) { }

	@Override public void exitRecordSection(PascalParser.RecordSectionContext ctx) { }

	@Override public void enterVariantPart(PascalParser.VariantPartContext ctx) { }

	@Override public void exitVariantPart(PascalParser.VariantPartContext ctx) { }

	@Override public void enterTag(PascalParser.TagContext ctx) { }

	@Override public void exitTag(PascalParser.TagContext ctx) { }

	@Override public void enterVariant(PascalParser.VariantContext ctx) { }

	@Override public void exitVariant(PascalParser.VariantContext ctx) { }

	@Override public void enterSetType(PascalParser.SetTypeContext ctx) { }

	@Override public void exitSetType(PascalParser.SetTypeContext ctx) { }

	@Override public void enterBaseType(PascalParser.BaseTypeContext ctx) { }

	@Override public void exitBaseType(PascalParser.BaseTypeContext ctx) { }

	@Override public void enterFileType(PascalParser.FileTypeContext ctx) { }

	@Override public void exitFileType(PascalParser.FileTypeContext ctx) { }

	@Override public void enterPointerType(PascalParser.PointerTypeContext ctx) { }

	@Override public void exitPointerType(PascalParser.PointerTypeContext ctx) { }

	@Override public void enterVariableDeclarationPart(PascalParser.VariableDeclarationPartContext ctx) { }

	@Override public void exitVariableDeclarationPart(PascalParser.VariableDeclarationPartContext ctx) { }

	@Override public void enterVariableDeclaration(PascalParser.VariableDeclarationContext ctx) { }

	@Override public void exitVariableDeclaration(PascalParser.VariableDeclarationContext ctx) { }

	@Override public void enterProcedureAndFunctionDeclarationPart(PascalParser.ProcedureAndFunctionDeclarationPartContext ctx) { }

	@Override public void exitProcedureAndFunctionDeclarationPart(PascalParser.ProcedureAndFunctionDeclarationPartContext ctx) { }

	@Override public void enterProcedureOrFunctionDeclaration(PascalParser.ProcedureOrFunctionDeclarationContext ctx) { }

	@Override public void exitProcedureOrFunctionDeclaration(PascalParser.ProcedureOrFunctionDeclarationContext ctx) { }

	@Override public void enterProcedureDeclaration(PascalParser.ProcedureDeclarationContext ctx) { }

	@Override public void exitProcedureDeclaration(PascalParser.ProcedureDeclarationContext ctx) { }

	@Override public void enterFormalParameterList(PascalParser.FormalParameterListContext ctx) { }

	@Override public void exitFormalParameterList(PascalParser.FormalParameterListContext ctx) { }

	@Override public void enterFormalParameterSection(PascalParser.FormalParameterSectionContext ctx) { }

	@Override public void exitFormalParameterSection(PascalParser.FormalParameterSectionContext ctx) { }

	@Override public void enterParameterGroup(PascalParser.ParameterGroupContext ctx) { }

	@Override public void exitParameterGroup(PascalParser.ParameterGroupContext ctx) { }

	@Override public void enterIdentifierList(PascalParser.IdentifierListContext ctx) { }

	@Override public void exitIdentifierList(PascalParser.IdentifierListContext ctx) { }

	@Override public void enterConstList(PascalParser.ConstListContext ctx) { }

	@Override public void exitConstList(PascalParser.ConstListContext ctx) { }

	@Override public void enterFunctionDeclaration(PascalParser.FunctionDeclarationContext ctx) { }

	@Override public void exitFunctionDeclaration(PascalParser.FunctionDeclarationContext ctx) { }

	@Override public void enterResultType(PascalParser.ResultTypeContext ctx) { }

	@Override public void exitResultType(PascalParser.ResultTypeContext ctx) { }

	@Override public void enterStatement(PascalParser.StatementContext ctx) { }

	@Override public void exitStatement(PascalParser.StatementContext ctx) { }

	@Override public void enterUnlabelledStatement(PascalParser.UnlabelledStatementContext ctx) { }

	@Override public void exitUnlabelledStatement(PascalParser.UnlabelledStatementContext ctx) { }

	@Override public void enterSimpleStatement(PascalParser.SimpleStatementContext ctx) { }

	@Override public void exitSimpleStatement(PascalParser.SimpleStatementContext ctx) { }

	@Override public void enterAssignmentStatement(PascalParser.AssignmentStatementContext ctx) { }

	@Override public void exitAssignmentStatement(PascalParser.AssignmentStatementContext ctx) { }

	@Override public void enterVariable(PascalParser.VariableContext ctx) { }

	@Override public void exitVariable(PascalParser.VariableContext ctx) { }

	@Override public void enterExpression(PascalParser.ExpressionContext ctx) { }

	@Override public void exitExpression(PascalParser.ExpressionContext ctx) { }

	@Override public void enterRelationaloperator(PascalParser.RelationaloperatorContext ctx) { }

	@Override public void exitRelationaloperator(PascalParser.RelationaloperatorContext ctx) { }

	@Override public void enterSimpleExpression(PascalParser.SimpleExpressionContext ctx) { }

	@Override public void exitSimpleExpression(PascalParser.SimpleExpressionContext ctx) { }

	@Override public void enterAdditiveoperator(PascalParser.AdditiveoperatorContext ctx) { }

	@Override public void exitAdditiveoperator(PascalParser.AdditiveoperatorContext ctx) { }

	@Override public void enterTerm(PascalParser.TermContext ctx) { }

	@Override public void exitTerm(PascalParser.TermContext ctx) { }

	@Override public void enterMultiplicativeoperator(PascalParser.MultiplicativeoperatorContext ctx) { }

	@Override public void exitMultiplicativeoperator(PascalParser.MultiplicativeoperatorContext ctx) { }

	@Override public void enterSignedFactor(PascalParser.SignedFactorContext ctx) { }

	@Override public void exitSignedFactor(PascalParser.SignedFactorContext ctx) { }

	@Override public void enterFactor(PascalParser.FactorContext ctx) { }

	@Override public void exitFactor(PascalParser.FactorContext ctx) { }

	@Override public void enterUnsignedConstant(PascalParser.UnsignedConstantContext ctx) { }

	@Override public void exitUnsignedConstant(PascalParser.UnsignedConstantContext ctx) { }

	@Override public void enterFunctionDesignator(PascalParser.FunctionDesignatorContext ctx) { }

	@Override public void exitFunctionDesignator(PascalParser.FunctionDesignatorContext ctx) { }

	@Override public void enterParameterList(PascalParser.ParameterListContext ctx) { }

	@Override public void exitParameterList(PascalParser.ParameterListContext ctx) { }

	@Override public void enterSet_(PascalParser.Set_Context ctx) { }

	@Override public void exitSet_(PascalParser.Set_Context ctx) { }

	@Override public void enterElementList(PascalParser.ElementListContext ctx) { }

	@Override public void exitElementList(PascalParser.ElementListContext ctx) { }

	@Override public void enterElement(PascalParser.ElementContext ctx) { }

	@Override public void exitElement(PascalParser.ElementContext ctx) { }

	@Override public void enterProcedureStatement(PascalParser.ProcedureStatementContext ctx) { }

	@Override public void exitProcedureStatement(PascalParser.ProcedureStatementContext ctx) { }

	@Override public void enterActualParameter(PascalParser.ActualParameterContext ctx) { }

	@Override public void exitActualParameter(PascalParser.ActualParameterContext ctx) { }

	@Override public void enterParameterwidth(PascalParser.ParameterwidthContext ctx) { }

	@Override public void exitParameterwidth(PascalParser.ParameterwidthContext ctx) { }

	@Override public void enterGotoStatement(PascalParser.GotoStatementContext ctx) { }

	@Override public void exitGotoStatement(PascalParser.GotoStatementContext ctx) { }

	@Override public void enterEmptyStatement(PascalParser.EmptyStatementContext ctx) { }

	@Override public void exitEmptyStatement(PascalParser.EmptyStatementContext ctx) { }

	@Override public void enterEmpty_(PascalParser.Empty_Context ctx) { }

	@Override public void exitEmpty_(PascalParser.Empty_Context ctx) { }

	@Override public void enterStructuredStatement(PascalParser.StructuredStatementContext ctx) { }

	@Override public void exitStructuredStatement(PascalParser.StructuredStatementContext ctx) { }

	@Override public void enterCompoundStatement(PascalParser.CompoundStatementContext ctx) { }

	@Override public void exitCompoundStatement(PascalParser.CompoundStatementContext ctx) { }

	@Override public void enterStatements(PascalParser.StatementsContext ctx) { }

	@Override public void exitStatements(PascalParser.StatementsContext ctx) { }

	@Override public void enterConditionalStatement(PascalParser.ConditionalStatementContext ctx) { }

	@Override public void exitConditionalStatement(PascalParser.ConditionalStatementContext ctx) { }

	@Override public void enterIfStatement(PascalParser.IfStatementContext ctx) { }

	@Override public void exitIfStatement(PascalParser.IfStatementContext ctx) { }

	@Override public void enterCaseStatement(PascalParser.CaseStatementContext ctx) { }

	@Override public void exitCaseStatement(PascalParser.CaseStatementContext ctx) { }

	@Override public void enterCaseListElement(PascalParser.CaseListElementContext ctx) { }

	@Override public void exitCaseListElement(PascalParser.CaseListElementContext ctx) { }

	@Override public void enterRepetetiveStatement(PascalParser.RepetetiveStatementContext ctx) { }

	@Override public void exitRepetetiveStatement(PascalParser.RepetetiveStatementContext ctx) { }

	@Override public void enterWhileStatement(PascalParser.WhileStatementContext ctx) { }

	@Override public void exitWhileStatement(PascalParser.WhileStatementContext ctx) { }

	@Override public void enterRepeatStatement(PascalParser.RepeatStatementContext ctx) { }

	@Override public void exitRepeatStatement(PascalParser.RepeatStatementContext ctx) { }

	@Override public void enterForStatement(PascalParser.ForStatementContext ctx) { }

	@Override public void exitForStatement(PascalParser.ForStatementContext ctx) { }

	@Override public void enterForList(PascalParser.ForListContext ctx) { }

	@Override public void exitForList(PascalParser.ForListContext ctx) { }

	@Override public void enterInitialValue(PascalParser.InitialValueContext ctx) { }

	@Override public void exitInitialValue(PascalParser.InitialValueContext ctx) { }

	@Override public void enterFinalValue(PascalParser.FinalValueContext ctx) { }

	@Override public void exitFinalValue(PascalParser.FinalValueContext ctx) { }

	@Override public void enterWithStatement(PascalParser.WithStatementContext ctx) { }

	@Override public void exitWithStatement(PascalParser.WithStatementContext ctx) { }

	@Override public void enterRecordVariableList(PascalParser.RecordVariableListContext ctx) { }

	@Override public void exitRecordVariableList(PascalParser.RecordVariableListContext ctx) { }

	@Override public void enterEveryRule(ParserRuleContext ctx) { }

	@Override public void exitEveryRule(ParserRuleContext ctx) { }

	@Override public void visitTerminal(TerminalNode node) { }

	@Override public void visitErrorNode(ErrorNode node) {
		//System.out.println(node.toString());
	}
}