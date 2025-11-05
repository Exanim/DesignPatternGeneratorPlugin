package com.github.exanim.designpatterngeneratorplugin.patterns.impl.composite

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Composite Meta-Pattern Generator: Command + Memento
 *
 * Common Use case: Undoable operations (text editor, drawing tool, IDE refactor steps).
 * Combination of two patterns:
 * 1. Command encapsulates a user action (execute / undo) and decouples invoker from receiver.
 * 2. Memento captures immutable snapshots of the receiver's state for restoration during undo.
 *
 * Structure generated:
 * - Receiver (e.g., Document) whose state changes.
 * - Memento encapsulating receiver state.
 * - Caretaker managing a stack of mementos (undo history).
 * - Command interface (execute + undo).
 * - One or two Concrete Commands (e.g., InsertTextCommand, RemoveTextCommand).
 * - Optional Client demo wiring receiver, caretaker, and commands.
 */
class UndoableCommandCompositePatternGenerator : BaseJavaPatternGenerator() {
    override val id: String = "undoablecommand"
    override val nameKey: String = "pattern.undoablecommand.name"
    override val descriptionKey: String = "pattern.undoablecommand.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("receiverClassName", "pattern.parameters.receiverClassName", ParameterType.STRING, required = true, defaultValue = "Document"),
        PatternParameter("mementoClassName", "pattern.parameters.mementoClassName", ParameterType.STRING, required = true, defaultValue = "DocumentMemento"),
        PatternParameter("caretakerClassName", "pattern.parameters.caretakerClassName", ParameterType.STRING, required = true, defaultValue = "Caretaker"),
        PatternParameter("commandInterfaceName", "pattern.parameters.commandInterfaceName", ParameterType.STRING, required = true, defaultValue = "Command"),
        PatternParameter("firstConcreteCommandName", "pattern.parameters.firstConcreteCommandName", ParameterType.STRING, required = true, defaultValue = "InsertTextCommand"),
        PatternParameter("secondConcreteCommandName", "pattern.parameters.secondConcreteCommandName", ParameterType.STRING, required = false, defaultValue = "RemoveTextCommand"),
        PatternParameter("executeMethodName", "pattern.parameters.executeMethodName", ParameterType.STRING, required = true, defaultValue = "execute"),
        PatternParameter("undoMethodName", "pattern.parameters.undoMethodName", ParameterType.STRING, required = true, defaultValue = "undo"),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false, defaultValue = "UndoDemo")
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val receiver = values["receiverClassName"] as? String ?: return emptyList()
        val memento = values["mementoClassName"] as? String ?: return emptyList()
        val caretaker = values["caretakerClassName"] as? String ?: return emptyList()
        val commandIface = values["commandInterfaceName"] as? String ?: return emptyList()
        val cmd1 = values["firstConcreteCommandName"] as? String ?: return emptyList()
        val cmd2 = (values["secondConcreteCommandName"] as? String)?.takeIf { it.isNotBlank() }
        val executeName = values["executeMethodName"] as? String ?: return emptyList()
        val undoName = values["undoMethodName"] as? String ?: return emptyList()
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()

        // Receiver
        val receiverSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Receiver (e.g., Document) whose state is mutated by Commands.")
            appendLine(" * Provides minimal operations used by commands and exposes memento creation/restoration.")
            appendLine(" */")
            appendLine("public class $receiver {")
            appendLine("    private final StringBuilder content = new StringBuilder();")
            appendLine("    /** Appends text to the document */")
            appendLine("    public void append(String text){ content.append(text); }")
            appendLine("    /** Removes last n characters if possible */")
            appendLine("    public void removeLast(int n){ int len = content.length(); if(n <= len){ content.delete(len - n, len); } }")
            appendLine("    /** Creates a memento snapshot */")
            appendLine("    public $memento createMemento(){ return new $memento(content.toString()); }")
            appendLine("    /** Restores state from memento */")
            appendLine("    public void restore($memento m){ content.setLength(0); content.append(m.getState()); }")
            appendLine("    @Override public String toString(){ return content.toString(); }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$receiver.java", receiverSrc, pkg)

        // Memento
        val mementoSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Immutable Memento capturing receiver state */")
            appendLine("public final class $memento {")
            appendLine("    private final String state;")
            appendLine("    public $memento(String state){ this.state = state; }")
            appendLine("    public String getState(){ return state; }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$memento.java", mementoSrc, pkg)

        // Caretaker
        val caretakerSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("import java.util.*;")
            appendLine("/** Caretaker managing undo history stack of Mementos */")
            appendLine("public class $caretaker {")
            appendLine("    private final Deque<$memento> history = new ArrayDeque<>();")
            appendLine("    public void push($memento m){ history.push(m); }")
            appendLine("    public $memento pop(){ return history.isEmpty() ? null : history.pop(); }")
            appendLine("    public boolean hasHistory(){ return !history.isEmpty(); }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$caretaker.java", caretakerSrc, pkg)

        // Command interface
        val commandIfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Command interface encapsulates a user action on the Receiver.")
            appendLine(" * execute(): perform the action (saves memento BEFORE mutation).\n * undo(): revert using caretaker stored mementos.")
            appendLine(" */")
            appendLine("public interface $commandIface {")
            appendLine("    void $executeName();")
            appendLine("    void $undoName();")
            appendLine("}")
        }
        files += JavaGeneratedFile("$commandIface.java", commandIfaceSrc, pkg)

        fun concreteCommandSource(name: String, behavior: String, isRemove: Boolean): String = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/**")
            appendLine(" * Concrete Command: $name\n * Behavior: $behavior\n * Saves a memento before mutating the receiver so undo can restore previous state.")
            appendLine(" */")
            appendLine("public class $name implements $commandIface {")
            appendLine("    private final $receiver receiver;")
            appendLine("    private final $caretaker caretaker;")
            if(!isRemove) appendLine("    private final String text;") else appendLine("    private final int count;")
            appendLine("    public $name($receiver receiver, $caretaker caretaker, ${if(isRemove) "int count" else "String text"}) {")
            appendLine("        this.receiver = receiver; this.caretaker = caretaker; ${if(isRemove) "this.count = count;" else "this.text = text;"}")
            appendLine("    }")
            appendLine("    @Override public void $executeName(){")
            appendLine("        caretaker.push(receiver.createMemento());")
            appendLine(if(isRemove) "        receiver.removeLast(count);" else "        receiver.append(text);")
            appendLine("    }")
            appendLine("    @Override public void $undoName(){")
            appendLine("        $memento m = caretaker.pop();")
            appendLine("        if(m != null){ receiver.restore(m); }")
            appendLine("    }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$cmd1.java", concreteCommandSource(cmd1, "Insert text", false), pkg)
        cmd2?.let { second ->
            files += JavaGeneratedFile("$second.java", concreteCommandSource(second, "Remove characters", true), pkg)
        }

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/**")
                appendLine(" * Client demo executing two commands and performing undo operations.")
                appendLine(" */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args){")
                appendLine("        $receiver doc = new $receiver();")
                appendLine("        $caretaker history = new $caretaker();")
                appendLine("        $commandIface insertHello = new $cmd1(doc, history, \"Hello \" );")
                appendLine("        insertHello.$executeName();")
                appendLine("        System.out.println(doc); // Hello ")
                appendLine("        $commandIface insertWorld = new $cmd1(doc, history, \"World!\" );")
                appendLine("        insertWorld.$executeName();")
                appendLine("        System.out.println(doc); // Hello World!")
                cmd2?.let {
                    appendLine("        $commandIface removeLast = new $it(doc, history, 6); // remove 'World!'")
                    appendLine("        removeLast.$executeName();")
                    appendLine("        System.out.println(doc); // Hello ")
                    appendLine("        removeLast.$undoName();")
                    appendLine("        System.out.println(doc); // Hello World!")
                }
                appendLine("        insertWorld.$undoName();")
                appendLine("        System.out.println(doc); // Hello ")
                appendLine("        insertHello.$undoName();")
                appendLine("        System.out.println(doc); // (empty)")
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }

        return files
    }
}
