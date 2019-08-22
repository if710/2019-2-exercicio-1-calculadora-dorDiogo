package br.ufpe.cin.android.calculadora

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

const val EXPRESSION_STATE = "expression_state"
const val RESULT_STATE = "result_state"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Possivelmente restaura estado da activity.
        text_calc.setText(savedInstanceState?.getString(EXPRESSION_STATE, ""))
        text_info.text = savedInstanceState?.getString(RESULT_STATE, "")

        // Cria listeners dos botões.
        createEqualButtonListener()
        createClearButtonListener()
        createExpressionButtonsListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(EXPRESSION_STATE, text_calc.text.toString())
        outState.putString(RESULT_STATE, text_info.text.toString())
        super.onSaveInstanceState(outState)
    }
    // Avalia a expressão. Se expressão válida, mostra a resposta no campo text_info.
    // Caso contrário, mostra uma Toast com a exceção.
    fun createEqualButtonListener() {
        btn_Equal.setOnClickListener {
            try {
                val answer = eval(text_calc.text.toString())
                text_info.text = answer.toString()
            } catch (e: RuntimeException) {
                Toast.makeText(
                    this,
                    e.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
            // Reseta campo de expressão, após ser calculada (ou inválida).
            resetExpression()
        }
    }

    // Reseta campo de expressão, se o botão 'C' é clicado.
    fun createClearButtonListener() {
        btn_Clear.setOnClickListener { resetExpression() }
    }

    // Cria um listener pros botões que constroem uma expressão. Adiciona o
    // respectivo caracter ao campo da expressão.
    fun createExpressionButtonsListeners() {
        arrayOf(
            btn_0, btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9, btn_Add,
            btn_Divide, btn_Dot, btn_LParen, btn_Multiply, btn_Power, btn_RParen, btn_Subtract
        )
            .map { buttonListener ->
                buttonListener.setOnClickListener {
                    text_calc.text.append(buttonListener.text)
                }
            }
    }

    fun resetExpression() {
        text_calc.text.clear()
    }


    //Como usar a função:
    // eval("2+2") == 4.0
    // eval("2+3*4") = 14.0
    // eval("(2+3)*4") = 20.0
    //Fonte: https://stackoverflow.com/a/26227947
    fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch: Char = ' '
            fun nextChar() {
                val size = str.length
                ch = if ((++pos < size)) str.get(pos) else (-1).toChar()
            }

            fun eat(charToEat: Char): Boolean {
                while (ch == ' ') nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Caractere inesperado: " + ch)
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            // | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'))
                        x += parseTerm() // adição
                    else if (eat('-'))
                        x -= parseTerm() // subtração
                    else
                        return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'))
                        x *= parseFactor() // multiplicação
                    else if (eat('/'))
                        x /= parseFactor() // divisão
                    else
                        return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+')) return parseFactor() // + unário
                if (eat('-')) return -parseFactor() // - unário
                var x: Double
                val startPos = this.pos
                if (eat('(')) { // parênteses
                    x = parseExpression()
                    eat(')')
                } else if ((ch in '0'..'9') || ch == '.') { // números
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else if (ch in 'a'..'z') { // funções
                    while (ch in 'a'..'z') nextChar()
                    val func = str.substring(startPos, this.pos)
                    x = parseFactor()
                    if (func == "sqrt")
                        x = Math.sqrt(x)
                    else if (func == "sin")
                        x = Math.sin(Math.toRadians(x))
                    else if (func == "cos")
                        x = Math.cos(Math.toRadians(x))
                    else if (func == "tan")
                        x = Math.tan(Math.toRadians(x))
                    else
                        throw RuntimeException("Função desconhecida: " + func)
                } else {
                    throw RuntimeException("Caractere inesperado: " + ch.toChar())
                }
                if (eat('^')) x = Math.pow(x, parseFactor()) // potência
                return x
            }
        }.parse()
    }
}
