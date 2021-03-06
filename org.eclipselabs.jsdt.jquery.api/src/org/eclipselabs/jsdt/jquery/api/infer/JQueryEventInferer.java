/*
 * *****************************************************************************
 * Copyright (c) 2011 Philippe Marschall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Philippe Marschall
 * *****************************************************************************
 */
package org.eclipselabs.jsdt.jquery.api.infer;

import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IArgument;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFunctionCall;
import org.eclipse.wst.jsdt.core.ast.IFunctionExpression;
import org.eclipse.wst.jsdt.core.infer.InferredType;


class JQueryEventInferer extends JQueryInferrerSupport {

  private static final InferredType jQueryEvent;

  private final JQueryCallbackMethods callbackMethods;

  static {
    char[] selector = "jQueryEvent".toCharArray();
    jQueryEvent = new InferredType(selector);
  }

  JQueryEventInferer(JQueryCallbackMethods callbackMethods, boolean noConflict) {
    super(noConflict);
    this.callbackMethods = callbackMethods;
  }

  @Override
  public boolean visit(IFunctionCall functionCall) {

    IExpression receiver = functionCall.getReceiver();
    if (this.isJQueryObject(receiver)) {
      String selector = new String(functionCall.getSelector());
      if (this.isJQueryEventSelector(selector)) {
        this.inferJQueryFunctionCall(functionCall, selector);
      }
    }
    return super.visit(functionCall);
  }

  private void inferJQueryFunctionCall(IFunctionCall functionCall, String selector) {
    IExpression[] functionCallArguments = functionCall.getArguments();
    if (functionCallArguments != null) {
      int argumentCount = functionCallArguments.length;
      Set<Integer> callbackIndices = this.getCallbackIndices(selector, argumentCount);
      for (int i = 0; i < argumentCount; ++i) {
        IExpression expression = functionCallArguments[i];
        if (expression.getASTType() == IASTNode.FUNCTION_EXPRESSION && callbackIndices.contains(i)) {
          this.setInferredTypeEvent(expression);
        }
      }
    }
  }

  private void setInferredTypeEvent(IExpression expression) {
    IFunctionExpression functionExpression = (IFunctionExpression) expression;
    IArgument[] functionExpressionArguments = functionExpression.getMethodDeclaration().getArguments();
    if (functionExpressionArguments != null && functionExpressionArguments.length == 1) {
      IArgument argument = functionExpressionArguments[0];
      argument.setInferredType(jQueryEvent);
    }
  }

  private Set<Integer> getCallbackIndices(String selector, int argumentCount) {
    return this.callbackMethods.getCallbackIndices(selector, argumentCount);
  }

  private boolean isJQueryEventSelector(String selector) {
    return this.callbackMethods.isEventSelector(selector);
  }

}
