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
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * This plugin adds toString() method to the generated model and example classes.
 * 
 * @author Iwao AVE!
 */
public class ToStringPlugin extends PluginAdapter
{

	public boolean validate(List<String> warnings)
	{
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateToString(introspectedTable, topLevelClass);
		return true;
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateToString(introspectedTable, topLevelClass);
		return true;
	}

	@Override
	public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateToString(introspectedTable, topLevelClass);
		return true;
	}

	private void generateToString(IntrospectedTable introspectedTable, InnerClass innerClass)
	{
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getStringInstance());
		method.setName("toString");
		if (introspectedTable.isJava5Targeted())
		{
			method.addAnnotation("@Override");
		}

		if (innerClass instanceof TopLevelClass)
		{
			context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		}

		method.addBodyLine("StringBuilder sb = new StringBuilder();");
		method.addBodyLine("sb.append(getClass().getName());");
		method.addBodyLine("sb.append(\"@\");");
		method.addBodyLine("sb.append(Integer.toHexString(System.identityHashCode(this)));");
		method.addBodyLine("sb.append(\"[\");");
		method.addBodyLine("sb.append(super.toString());");

		StringBuilder sb = new StringBuilder();
		for (Field field : innerClass.getFields())
		{
			String property = field.getName();
			sb.setLength(0);
			sb.append("sb.append(\",")
				.append(property)
				.append("=\")")
				.append(".append(")
				.append(property)
				.append(");");
			method.addBodyLine(sb.toString());
		}

		method.addBodyLine("sb.append(\"]\");");
		method.addBodyLine("return sb.toString();");

		innerClass.addMethod(method);
	}

}
