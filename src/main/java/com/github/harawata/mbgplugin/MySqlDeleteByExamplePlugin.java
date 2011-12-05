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

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * When you (1) use MySQL as a data source and (2) configure mybatis generator to use an alias
 * for a table name, the default implementation of deleteByExample results in an syntax error
 * because of <a href="http://bugs.mysql.com/bug.php?id=12811">MySQL Bug #12811</a>.<br>
 * This plugin tweaks the DELETE statement to workaround the issue.
 * 
 * @author Iwao AVE!
 */
public class MySqlDeleteByExamplePlugin extends PluginAdapter
{

	public boolean validate(List<String> warnings)
	{
		return true;
	}

	@Override
	public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element,
		IntrospectedTable introspectedTable)
	{
		FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
		for (int i = 0; i < element.getElements().size(); i++)
		{
			Element elem = element.getElements().get(i);
			if (elem instanceof TextElement
				&& elem.getFormattedContent(0).contains("delete from "))
				replaceDeleteStatement(element, table, i);
		}
		return true;
	}

	private void replaceDeleteStatement(XmlElement element, FullyQualifiedTable table, int index)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		String alias = table.getAlias();
		if (alias != null && alias.length() > 0)
		{
			sb.append(alias);
			sb.append(" using ");
		}
		sb.append(table.getAliasedFullyQualifiedTableNameAtRuntime());

		element.getElements().set(index, new TextElement(sb.toString()));
	}
}
