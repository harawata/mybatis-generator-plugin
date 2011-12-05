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
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * The default implementaiton of addCriterion() methods throw exception when the passed value(s)
 * are <code>null</code>.<br>
 * This could be redundant if you do null-checks in your presentation layer for example.<br>
 * This plugin changes the behavior of addCriterion methods to ignore <code>null</code> values.<br>
 * Note that if all the parameters are <code>null</code>, all the rows will be affected by
 * xxxByExample methods.
 * 
 * @author Iwao AVE!
 */
public class InsensitveAddCriterionPlugin extends PluginAdapter
{

	public boolean validate(List<String> warnings)
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
		}
		return true;
	}

	private void modifyGeneratedCriteria(IntrospectedTable introspectedTable,
		InnerClass innerClass)
	{
		Iterator<Method> it = innerClass.getMethods().iterator();
		while (it.hasNext())
		{
			Method m = it.next();
			String methodName = m.getName();
			if (methodName.startsWith("addCriterion"))
			{
				replaceRuntimeException(m);
			}
		}
	}

	/**
	 * Searches/replaces a line throwing exception in the passed method.
	 */
	private void replaceRuntimeException(Method m)
	{
		List<String> bodyLines = m.getBodyLines();
		for (int i = 0; i < bodyLines.size(); i++)
		{
			if (bodyLines.get(i).contains("throw new RuntimeException"))
			{
				bodyLines.set(i, "return;");
				break;
			}
		}
	}

}
