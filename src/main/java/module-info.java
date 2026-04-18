/**
 * @author VISTALL
 * @since 31/05/2023
 */
open module consulo.psiviewer
{
	requires consulo.application.api;
	requires consulo.base.icon.library;
	requires consulo.code.editor.api;
	requires consulo.color.scheme.api;
	requires consulo.component.api;
	requires consulo.document.api;
	requires consulo.file.editor.api;
	requires consulo.language.api;
	requires consulo.language.editor.api;
	requires consulo.language.impl;
	requires consulo.localize.api;
	requires consulo.logging.api;
	requires consulo.navigation.api;
	requires consulo.project.api;
	requires consulo.project.ui.api;
	requires consulo.ui.api;
	requires consulo.ui.ex.api;
	requires consulo.ui.ex.awt.api;
	requires consulo.util.collection;
	requires consulo.util.lang;
	requires consulo.virtual.file.system.api;

	// TODO remove in future
	requires java.desktop;
}
