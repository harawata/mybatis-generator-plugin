/*-
 *  Copyright 2011 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.harawata.mbgplugin;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * This plugin adds addCriterionLeftValue() method to the GeneratedCriteria inner class of the
 * example class.<br>
 * This allows you to add a condition like <code>? in (col_a, col_b, col_c)</code> by adding a
 * new method like
 * <code>addCriterionLeftValue("in (col_a, col_b, col_c)", value, "propertyName")</code> to the
 * Criteria inner class of the generated example class.
 * 
 * @author Iwao AVE!
 */
public class LeftValueCriteriaPlugin extends PluginAdapter
{

	public boolean validate(List<String> arg0)
	{
		return true;
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		for (InnerClass innerClass : topLevelClass.getInnerClasses())
		{
			if ("GeneratedCriteria".equals(innerClass.getType().getShortName()))
			{
				modifyGeneratedCriteria(introspectedTable, innerClass);
			}
			else if ("Criterion".equals(innerClass.getType().getShortName()))
			{
				modifyCriterion(introspectedTable, innerClass);
			}
		}
		return true;
	}

	private void modifyCriterion(IntrospectedTable introspectedTable, InnerClass innerClass)
	{
		Field field = new Field();
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
		field.setName("leftValue");
		innerClass.addField(field);

		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("isLeftValue");
		method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
		method.addBodyLine("return leftValue;");
		innerClass.addMethod(method);

		Method constructor1 = new Method();
		constructor1.setConstructor(true);
		constructor1.setName("Criterion");
		constructor1.setVisibility(JavaVisibility.PROTECTED);
		constructor1.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(),
			"condition"));
		constructor1.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(),
			"value"));
		constructor1.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(),
			"typeHandler"));
		constructor1.addParameter(new Parameter(
			FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "isLeftValue"));
		constructor1.addBodyLine("super();");
		constructor1.addBodyLine("this.condition = condition;");
		constructor1.addBodyLine("this.value = value;");
		constructor1.addBodyLine("this.typeHandler = typeHandler;");
		constructor1.addBodyLine("if (isLeftValue){ this.leftValue = true; }");
		constructor1.addBodyLine("else if (value instanceof List<?>){ this.listValue = true; }");
		constructor1.addBodyLine("else {this.singleValue = true;}");
		innerClass.addMethod(constructor1);

		Method constructor2 = new Method();
		constructor2.setConstructor(true);
		constructor2.setName("Criterion");
		constructor2.setVisibility(JavaVisibility.PROTECTED);
		constructor2.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(),
			"condition"));
		constructor2.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(),
			"value"));
		constructor2.addParameter(new Parameter(
			FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "isLeftValue"));
		constructor2.addBodyLine("this(condition, value, null, isLeftValue);");
		innerClass.addMethod(constructor2);
	}

	private void modifyGeneratedCriteria(IntrospectedTable introspectedTable,
		InnerClass innerClass)
	{
		Method method = new Method();
		method.setVisibility(JavaVisibility.PROTECTED);
		method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(),
			"condition"));
		method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
		method.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(),
			"property"));
		method.setName("addCriterionLeftValue");

		method.addBodyLine("if (value == null){");
		method.addBodyLine("throw new RuntimeException(\"Value for \" + property + \" cannot be null\");");
		method.addBodyLine("}");
		method.addBodyLine("criteria.add(new Criterion(condition, value, true));");

		innerClass.addMethod(method);
	}

	@Override
	public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element,
		IntrospectedTable introspectedTable)
	{
		// search 'choose' recursively.
		XmlElement chooseElement = findChoose(element);
		if (chooseElement != null)
			addElement(chooseElement);

		return true;
	}

	private XmlElement findChoose(XmlElement element)
	{
		if ("choose".equals(element.getName()))
			return element;

		for (Element child : element.getElements())
		{
			if (child instanceof XmlElement)
				return findChoose((XmlElement)child);
		}

		return null;
	}

	protected void addElement(XmlElement element)
	{
		XmlElement ifElement = new XmlElement("when");
		ifElement.addAttribute(new Attribute("test", "criterion.leftValue"));
		ifElement.addElement(new TextElement("and #{criterion.value} ${criterion.condition}"));

		element.addElement(ifElement);
	}

}
