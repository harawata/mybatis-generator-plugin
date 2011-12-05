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

import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * This plugin adds equals() and hashCode() methods to the generated java classes.<br>
 * Unlike the default(bundled) plugin, this plugin generates these methods for examples classes
 * as well.<br>
 * It could be useful when you use some kind of mocking framework to write unit tests.
 * 
 * @author Iwao AVE!
 */
public class EqualsHashCodePlugin extends PluginAdapter
{

	public boolean validate(List<String> warnings)
	{
		return true;
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateEquals(introspectedTable, topLevelClass);
		generateHashCode(introspectedTable, topLevelClass);
		return true;
	}

	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateEquals(introspectedTable, topLevelClass);
		generateHashCode(introspectedTable, topLevelClass);

		List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
		for (InnerClass innerClass : innerClasses)
		{
			String name = innerClass.getType().getShortName();
			if ("Criterion".equals(name) || "GeneratedCriteria".equals(name))
			{
				generateEquals(introspectedTable, innerClass);
				generateHashCode(introspectedTable, innerClass);
			}
		}

		return true;
	}

	@Override
	public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateEquals(introspectedTable, topLevelClass);
		generateHashCode(introspectedTable, topLevelClass);
		return true;
	}

	@Override
	public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass,
		IntrospectedTable introspectedTable)
	{
		generateEquals(introspectedTable, topLevelClass);
		generateHashCode(introspectedTable, topLevelClass);
		return true;
	}

	private void generateHashCode(IntrospectedTable introspectedTable, InnerClass innerClass)
	{
		Iterator<Field> iter;
		Method method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getIntInstance());
		method.setName("hashCode");
		if (introspectedTable.isJava5Targeted())
		{
			method.addAnnotation("@Override");
		}

		if (innerClass instanceof TopLevelClass)
		{
			context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		}

		method.addBodyLine("final int prime = 17;");
		if (hasSuperClass(innerClass))
			method.addBodyLine("int result = super.hashCode();");
		else
			method.addBodyLine("int result = 1;");

		StringBuilder sb = new StringBuilder();

		sb.setLength(0);
		boolean bitsDeclared = false;
		iter = innerClass.getFields().iterator();
		while (iter.hasNext())
		{
			Field field = iter.next();
			sb.setLength(0);

			String property = field.getName();
			FullyQualifiedJavaType type = field.getType();
			if (type.isPrimitive())
			{
				String typeStr = type.getFullyQualifiedName();
				if ("int".equals(typeStr))
				{
					sb.append("result = prime * result + ");
					sb.append(property);
					sb.append(";");
				}
				else if ("byte".equals(typeStr) || "char".equals(typeStr)
					|| "short".equals(typeStr))
				{
					sb.append("result = prime * result + (int)");
					sb.append(property);
					sb.append(";");
				}
				else if ("long".equals(typeStr))
				{
					sb.append("result = prime * result + (int)(");
					sb.append(property);
					sb.append(" ^ (");
					sb.append(property);
					sb.append(" >>> 32));");
				}
				else if ("float".equals(typeStr))
				{
					sb.append("result = prime * result + Float.floatToIntBits(");
					sb.append(property);
					sb.append(");");
				}
				else if ("double".equals(typeStr))
				{
					if (!bitsDeclared)
					{
						sb.append("long ");
						bitsDeclared = true;
					}
					sb.append("bits = Double.doubleToLongBits(");
					sb.append(property);
					sb.append(");");
					sb.append("result = prime * result + (int)(bits ^ (bits >>> 32));");
				}
				else if ("boolean".equals(typeStr))
				{
					sb.append("result = prime * result + (");
					sb.append(property);
					sb.append(" ? 1 : 0);");
				}
			}
			else
			{
				sb.append("result = prime * result + (null == ");
				sb.append(property);
				sb.append(" ? 0 : ");
				sb.append(property);
				sb.append(".hashCode());");
			}

			method.addBodyLine(sb.toString());
		}

		method.addBodyLine("return result;");

		innerClass.addMethod(method);
	}

	private void generateEquals(IntrospectedTable introspectedTable, InnerClass innerClass)
	{
		Method method;
		StringBuilder sb;
		boolean first;
		Iterator<Field> iter;
		method = new Method();
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
		method.setName("equals");
		method.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "that"));
		if (introspectedTable.isJava5Targeted())
		{
			method.addAnnotation("@Override");
		}

		if (innerClass instanceof TopLevelClass)
		{
			context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
		}

		method.addBodyLine("if (this == that)");
		method.addBodyLine("return true;");

		if (hasSuperClass(innerClass))
		{
			method.addBodyLine("if (!super.equals(that))");
			method.addBodyLine("return false;");
		}

		sb = new StringBuilder();
		sb.append("if((that == null) || (that.getClass() != this.getClass()))");
		method.addBodyLine(sb.toString());
		method.addBodyLine("return false;");

		sb.setLength(0);
		sb.append(innerClass.getType().getShortName());
		sb.append(" other = (");
		sb.append(innerClass.getType().getShortName());
		sb.append(") that;");
		method.addBodyLine(sb.toString());

		sb.setLength(0);
		first = true;
		iter = innerClass.getFields().iterator();
		while (iter.hasNext())
		{
			Field field = iter.next();
			sb.setLength(0);

			if (first)
			{
				sb.append("return ");
				first = false;
			}
			else
			{
				OutputUtilities.javaIndent(sb, 1);
				sb.append("&& ");
			}

			String property = field.getName();
			FullyQualifiedJavaType type = field.getType();
			if (type.isPrimitive())
			{
				sb.append(property);
				sb.append(" == other.");
				sb.append(property);
			}
			else
			{
				sb.append("(");
				sb.append(property);
				sb.append(" == other.");
				sb.append(property);
				sb.append(" || (");
				sb.append(property);
				sb.append(" != null && ");
				sb.append(property);
				sb.append(".equals(other.");
				sb.append(property);
				sb.append(")))");
			}

			if (!iter.hasNext())
			{
				sb.append(';');
			}

			method.addBodyLine(sb.toString());
		}
		innerClass.addMethod(method);
	}

	private boolean hasSuperClass(InnerClass innerClass)
	{
		return null != innerClass.getSuperClass();
	}

}
