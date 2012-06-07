package org.androidtransfuse.gen.componentBuilder;

import com.sun.codemodel.JMethod;
import org.androidtransfuse.analysis.adapter.ASTMethod;
import org.androidtransfuse.analysis.adapter.ASTParameter;
import org.androidtransfuse.model.TypedExpression;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class MethodDescriptor {

    private JMethod method;
    private Map<ASTParameter, TypedExpression> parameterMap = new HashMap<ASTParameter, TypedExpression>();
    private ASTMethod astMethod;

    public MethodDescriptor(JMethod method, ASTMethod astMethod) {
        this.method = method;
        this.astMethod = astMethod;
    }

    public JMethod getMethod() {
        return method;
    }

    public TypedExpression getParameter(ASTParameter astParameter) {
        return parameterMap.get(astParameter);
    }

    public void putParameter(ASTParameter astParameter, TypedExpression expression) {
        parameterMap.put(astParameter, expression);
    }

    public ASTMethod getASTMethod() {
        return astMethod;
    }

    public Map<ASTParameter, TypedExpression> getParameters() {
        return parameterMap;
    }
}
