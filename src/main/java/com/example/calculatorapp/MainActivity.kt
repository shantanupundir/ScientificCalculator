package com.example.calculatorapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    lateinit var tvsec: TextView
    lateinit var tvMain: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvsec = findViewById(R.id.idTVSecondary)
        tvMain = findViewById(R.id.idTVprimary)

        val buttons = listOf(
            R.id.b0, R.id.b1, R.id.b2, R.id.b3, R.id.b4, R.id.b5, R.id.b6, R.id.b7, R.id.b8, R.id.b9,
            R.id.bdot, R.id.bplus, R.id.bminus, R.id.bmul, R.id.bdiv, R.id.bbrac1, R.id.bbrac2,
            R.id.bpi, R.id.bsin, R.id.bcos, R.id.btan, R.id.bln, R.id.blog, R.id.binv
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                val text = (it as Button).text.toString()
                tvMain.text = tvMain.text.toString() + when (text) {
                    "π" -> {
                        tvsec.text = "π"
                        "3.142"
                    }
                    "x⁻¹" -> "^(-1)"
                    else -> text
                }
            }
        }

        findViewById<Button>(R.id.bac).setOnClickListener {
            tvMain.text = ""
            tvsec.text = ""
        }

        findViewById<Button>(R.id.bc).setOnClickListener {
            val str = tvMain.text.toString()
            if (str.isNotEmpty()) {
                tvMain.text = str.dropLast(1)
            }
        }

        findViewById<Button>(R.id.bsqrt).setOnClickListener {
            val str = tvMain.text.toString()
            if (str.isEmpty()) {
                toast("Please enter a valid number.")
            } else {
                try {
                    val r = sqrt(str.toDouble())
                    tvMain.text = r.toString()
                } catch (e: Exception) {
                    toast("Invalid input")
                }
            }
        }

        findViewById<Button>(R.id.bsquare).setOnClickListener {
            val str = tvMain.text.toString()
            if (str.isEmpty()) {
                toast("Please enter a valid number.")
            } else {
                try {
                    val d = str.toDouble()
                    val square = d * d
                    tvMain.text = square.toString()
                    tvsec.text = "$d²"
                } catch (e: Exception) {
                    toast("Invalid input")
                }
            }
        }

        findViewById<Button>(R.id.bfact).setOnClickListener {
            val str = tvMain.text.toString()
            if (str.isEmpty()) {
                toast("Please enter a valid number.")
            } else {
                try {
                    val value = str.toInt()
                    tvMain.text = factorial(value).toString()
                    tvsec.text = "$value!"
                } catch (e: Exception) {
                    toast("Only non-negative integers allowed.")
                }
            }
        }

        findViewById<Button>(R.id.bequal).setOnClickListener {
            val expr = tvMain.text.toString()
            try {
                val result = evaluate(expr)
                tvMain.text = result.toString()
                tvsec.text = expr
            } catch (e: Exception) {
                toast("Invalid Expression")
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun factorial(n: Int): Int {
        return if (n <= 1) 1 else n * factorial(n - 1)
    }

    private fun evaluate(expr: String): Double {
        return object {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected: ${ch.toChar()}")
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos

                when {
                    eat('('.code) -> {
                        x = parseExpression()
                        eat(')'.code)
                    }
                    ch in '0'.code..'9'.code || ch == '.'.code -> {
                        while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                        x = expr.substring(startPos, pos).toDouble()
                    }
                    ch in 'a'.code..'z'.code -> {
                        while (ch in 'a'.code..'z'.code) nextChar()
                        val func = expr.substring(startPos, pos)
                        x = parseFactor()
                        x = when (func) {
                            "sqrt" -> sqrt(x)
                            "sin" -> sin(Math.toRadians(x))
                            "cos" -> cos(Math.toRadians(x))
                            "tan" -> tan(Math.toRadians(x))
                            "log" -> log10(x)
                            "ln" -> ln(x)
                            else -> throw RuntimeException("Unknown function: $func")
                        }
                    }
                    else -> throw RuntimeException("Unexpected: ${ch.toChar()}")
                }

                if (eat('^'.code)) x = x.pow(parseFactor())

                return x
            }
        }.parse()
    }
}