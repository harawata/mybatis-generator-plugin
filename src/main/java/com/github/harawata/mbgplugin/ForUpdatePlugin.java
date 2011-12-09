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
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * This plugin adds setForUpdate(boolean) method to the example class.<br />
 * When it's enabled, 'for update' clause is appended to the select statement.
 * 
 * @author Iwao AVE!
 */
public class ForUpdatePlugin extends PluginAdapter
{

	public boolean validate(List<String> arg0)
	{
		return true;
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		// add field, getter, setter for for update clause
		Field field = new Field();
		field.setVisibility(JavaVisibility.PROTECTED);
		field.setType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
		field.setName("forUpdate");
		context.getCommentGenerator().addFieldComment(field, introspectedTable);
		topLevelClass.addField(field);

		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("setForUpdate");
		method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(),
			"forUpdate"));
		method.addBodyLine("this.forUpdate = forUpdate;");
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		topLevelClass.addMethod(method);

		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
		method.setName("isForUpdate");
		method.addBodyLine("return forUpdate;");
		topLevelClass.addMethod(method);
		context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

		return true;
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element,
		IntrospectedTable introspectedTable)
	{
		addElement(element);
		return true;
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element,
		IntrospectedTable introspectedTable)
	{
		addElement(element);
		return true;
	}

	protected void addElement(XmlElement element)
	{
		XmlElement ifElement = new XmlElement("if");
		ifElement.addAttribute(new Attribute("test", "forUpdate"));
		ifElement.addElement(new TextElement("for update"));
		element.addElement(ifElement);
	}

}
