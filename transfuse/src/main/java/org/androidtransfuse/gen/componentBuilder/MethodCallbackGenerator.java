/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.gen.componentBuilder;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JStatement;
import org.androidtransfuse.adapter.ASTJDefinedClassType;
import org.androidtransfuse.adapter.ASTMethod;
import org.androidtransfuse.adapter.ASTParameter;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.analysis.astAnalyzer.ListenerAspect;
import org.androidtransfuse.gen.InvocationBuilder;
import org.androidtransfuse.model.ComponentDescriptor;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.MethodDescriptor;
import org.androidtransfuse.model.TypedExpression;

import javax.inject.Inject;
import java.util.*;

/**
 * @author John Ericksen
 */
public class MethodCallbackGenerator implements ExpressionVariableDependentGenerator {

    private final ASTType eventAnnotation;
    private final MethodGenerator methodGenerator;
    private final InvocationBuilder invocationBuilder;
    //todo: this is a hack, need to build a better way to write methods
    private Generator generator = null;

    @Inject
    public MethodCallbackGenerator(/*@Assisted*/ ASTType eventAnnotation, /*@Assisted*/ MethodGenerator methodGenerator, InvocationBuilder invocationBuilder) {
        this.eventAnnotation = eventAnnotation;
        this.methodGenerator = methodGenerator;
        this.invocationBuilder = invocationBuilder;
    }

    public void generate(JDefinedClass definedClass, MethodDescriptor creationMethodDescriptor, Map<InjectionNode, TypedExpression> expressionMap, ComponentDescriptor descriptor, JExpression scopesExpression) {

        MethodDescriptor methodDescriptor = null;
        boolean calledOnce = false;
        JBlock body = null;
        for (Map.Entry<InjectionNode, TypedExpression> injectionNodeJExpressionEntry : expressionMap.entrySet()) {
            ListenerAspect methodCallbackAspect = injectionNodeJExpressionEntry.getKey().getAspect(ListenerAspect.class);

            if (methodCallbackAspect != null && methodCallbackAspect.contains(eventAnnotation)) {
                Set<ASTMethod> methods = methodCallbackAspect.getListeners(eventAnnotation);

                //define method on demand for possible lazy init
                if (methodDescriptor == null) {
                    methodDescriptor = methodGenerator.buildMethod(definedClass);
                    body = methodDescriptor.getMethod().body();
                }


                if(generator != null && !calledOnce && body != null){
                    body = generator.generate(definedClass, injectionNodeJExpressionEntry.getValue().getExpression(), body, methodDescriptor);
                    calledOnce = true;
                }

                for (ASTMethod methodCallback : methods) {

                    List<ASTParameter> matchedParameters = matchMethodArguments(methodDescriptor.getASTMethod().getParameters(), methodCallback);
                    List<JExpression> parameters = new ArrayList<JExpression>();

                    for (ASTParameter matchedParameter : matchedParameters) {
                        parameters.add(methodDescriptor.getParameters().get(matchedParameter).getExpression());
                    }

                    JStatement methodCall = invocationBuilder.buildMethodCall(
                            new ASTJDefinedClassType(definedClass),
                            new ASTJDefinedClassType(definedClass),
                            methodCallback,
                            parameters,
                            injectionNodeJExpressionEntry.getValue()
                    );

                    body.add(methodCall);
                }
            }
        }

        methodGenerator.closeMethod(methodDescriptor);
    }

    private List<ASTParameter> matchMethodArguments(List<ASTParameter> parametersToMatch, ASTMethod methodToCall) {
        List<ASTParameter> arguments = new ArrayList<ASTParameter>();

        List<ASTParameter> overrideParameters = new ArrayList<ASTParameter>(parametersToMatch);

        for (ASTParameter callParameter : methodToCall.getParameters()) {
            Iterator<ASTParameter> overrideParameterIterator = overrideParameters.iterator();

            while (overrideParameterIterator.hasNext()) {
                ASTParameter overrideParameter = overrideParameterIterator.next();
                if (overrideParameter.getASTType().equals(callParameter.getASTType())) {
                    arguments.add(overrideParameter);
                    overrideParameterIterator.remove();
                    break;
                }
            }
        }

        return arguments;
    }

    public interface Generator{
        JBlock generate(JDefinedClass definedClass, JExpression delegate, JBlock body, MethodDescriptor descriptor);
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }
}
