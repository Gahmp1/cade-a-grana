#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Contador Financeiro Pessoal - Análise de Extratos
Padrão Banco/Fintech
"""

import pandas as pd
import numpy as np
from pathlib import Path
from datetime import datetime
import re

# ==================== CONFIGURAÇÃO ====================
# Renda mensal manual (usado quando não há entradas no extrato)
# DESABILITADO - usando extratos bancários reais (Itaú + PicPay)
RENDA_MENSAL_MANUAL = 0.00  # Desativado temporariamente
USA_RENDA_MANUAL = False
# ======================================================

# Caminhos dos arquivos
arquivos_fatura = [
    "/Users/joaogabriel/Notas/Itau-fatura-20250606.csv",
    "/Users/joaogabriel/Notas/Itaufatura-20250706.csv",
    "/Users/joaogabriel/Notas/Itau-fatura-20250806.csv",
    "/Users/joaogabriel/Notas/Itau-fatura-20250906.csv",
    "/Users/joaogabriel/Notas/Itau-fatura-20251006.csv",
    "/Users/joaogabriel/Notas/Itau-fatura-20251106.csv",
    "/Users/joaogabriel/Notas/Itau-fatura-20260106.csv",
]

# Extratos bancários (contém rendas/entradas)
extrato_itau = "/Users/joaogabriel/Notas/Itau-Extrato Conta Corrente-090120261030.txt"
extrato_picpay = "/Users/joaogabriel/Notas/picpayExtrato.pdf"

# Dicionário de categorização automática
categorias_palavras_chave = {
    'Alimentação': [
        'restaurante', 'lanchonete', 'bar', 'cafe', 'pizza', 'hamburger', 'burger',
        'sushi', 'temakeria', 'food', 'ifood', 'mercado', 'supermercado', 'hortifruti',
        'padaria', 'confeitaria', 'acai', 'sorveteria', 'churrasco', 'grill',
        'buffet', 'refeicoes', 'lanches', 'fastfood', 'pastelaria', 'doce',
        'jtx', 'livantto', 'bullguer', 'mambo', 'outback', 'mc donalds',
        'emporio', 'drogaria', 'farmacia', 'haru', 'spaghetto'
    ],
    'Moradia': [
        'aluguel', 'condominio', 'luz', 'agua', 'gas', 'iptu', 'internet',
        'maranata gas'
    ],
    'Transporte': [
        'uber', 'taxi', 'gas', 'gasolina', 'posto', 'estacionamento', 'estac',
        'park', 'zul', 'metro', 'onibus', 'railway', 'latam', 'iberia',
        'automovel', 'auto posto', 'thrifty', 'tag'
    ],
    'Lazer': [
        'cinema', 'teatro', 'show', 'festa', 'bar', 'cerveja', 'chopp',
        'sesc', 'cinemark', 'entretenimento', 'shopping', 'campo sonhos',
        'applebees', 'cervejaria', 'cafe blue', 'starbucks'
    ],
    'Saúde': [
        'farmacia', 'drogaria', 'hospital', 'clinica', 'medico', 'laboratorio',
        'plano', 'pet health', 'veterinari', 'vet', 'saude', 'camargo cancer',
        'hosp', 'petland'
    ],
    'Educação': [
        'curso', 'livro', 'escola', 'faculdade', 'universidade', 'danki',
        'insider'
    ],
    'Assinaturas': [
        'netflix', 'spotify', 'amazon', 'apple', 'google', 'microsoft',
        'openai', 'chatgpt', 'youtube', 'gym', 'academia', 'clube',
        'assinatura', 'subscription', 'one', 'skyfit', 'suno'
    ],
    'Compras': [
        'mercadolivre', 'magazine', 'loja', 'shopping', 'riachuelo', 'havaianas',
        'carter', 'lojas', 'comercio', 'aliexpress', 'shein', 'granado',
        'cobasi', 'sonda', 'telha'
    ],
    'Taxas e Impostos': [
        'anuidade', 'tarifa', 'iof', 'imposto', 'taxa', 'seguro'
    ],
    'Transferências': [
        'pagamento', 'transferencia', 'pix', 'ted', 'doc', 'conectc'
    ]
}

def classificar_categoria(descricao):
    """Classifica automaticamente uma transação em categoria"""
    descricao_lower = descricao.lower()
    
    for categoria, palavras in categorias_palavras_chave.items():
        for palavra in palavras:
            if palavra in descricao_lower:
                return categoria
    
    return 'Outros'

def processar_extrato_itau(arquivo):
    """Processa o extrato do Itaú em formato TXT"""
    try:
        # Ler arquivo TXT com separador de ponto e vírgula
        df = pd.read_csv(arquivo, sep=';', header=None, names=['data', 'lançamento', 'valor'])
        
        # Converter data
        df['data'] = pd.to_datetime(df['data'], format='%d/%m/%Y', errors='coerce')
        
        # Converter valor (substituir vírgula por ponto)
        df['valor'] = df['valor'].astype(str).str.replace(',', '.').astype(float)
        
        # Adicionar informações de origem
        df['arquivo_origem'] = Path(arquivo).name
        df['fonte'] = 'extrato_itau'
        
        print(f"  → {len(df)} transações encontradas no extrato Itaú")
        
        return df
    except Exception as e:
        print(f"Erro ao processar extrato Itaú: {e}")
        return None

def processar_extrato_picpay(arquivo):
    """Processa o extrato do PicPay em formato PDF"""
    try:
        import pdfplumber
        
        transacoes = []
        
        with pdfplumber.open(arquivo) as pdf:
            for page in pdf.pages:
                text = page.extract_text()
                if not text:
                    continue
                
                lines = text.split('\n')
                
                for i, line in enumerate(lines):
                    # Procurar por padrões de data (dd/mm/yyyy)
                    if re.match(r'\d{2}/\d{2}/\d{4}', line):
                        try:
                            # Extrair data
                            data_str = line[:10]
                            
                            # Procurar descrição e valor na mesma linha ou próximas
                            descricao = ""
                            valor = 0.0
                            
                            # Tentar extrair descrição e valor
                            resto_linha = line[10:].strip()
                            
                            # Próxima linha pode ter a descrição completa
                            if i + 1 < len(lines):
                                proxima_linha = lines[i + 1]
                                
                                # Se a próxima linha tem descrição e valor
                                if 'R$' in proxima_linha:
                                    partes = proxima_linha.split('R$')
                                    descricao = partes[0].strip()
                                    
                                    # Extrair valores (pode ter múltiplos R$)
                                    valores_texto = [p.strip().split()[0] for p in partes[1:] if p.strip()]
                                    
                                    # Primeiro valor geralmente é o da transação
                                    if valores_texto:
                                        valor_str = valores_texto[0].replace('.', '').replace(',', '.')
                                        # Remover caracteres não numéricos exceto . e -
                                        valor_str = re.sub(r'[^\d.,-]', '', valor_str)
                                        
                                        try:
                                            valor = float(valor_str)
                                            
                                            # Se a descrição indica saída, tornar negativo
                                            if any(palavra in descricao.lower() for palavra in 
                                                   ['enviado', 'pagamento', 'aporte', 'débito']):
                                                valor = -abs(valor)
                                            # Se é entrada, tornar positivo
                                            elif any(palavra in descricao.lower() for palavra in 
                                                     ['recebid', 'crédito', 'rendimento', 'transferência recebida']):
                                                valor = abs(valor)
                                        except:
                                            continue
                            
                            if descricao and valor != 0.0:
                                transacoes.append({
                                    'data': data_str,
                                    'lançamento': descricao,
                                    'valor': valor
                                })
                        except Exception as e:
                            continue
        
        if not transacoes:
            print("  ⚠️ Nenhuma transação extraída do PicPay")
            return None
        
        # Criar DataFrame
        df = pd.DataFrame(transacoes)
        
        # Converter data
        df['data'] = pd.to_datetime(df['data'], format='%d/%m/%Y', errors='coerce')
        
        # Adicionar informações de origem
        df['arquivo_origem'] = Path(arquivo).name
        df['fonte'] = 'extrato_picpay'
        
        print(f"  → {len(df)} transações encontradas no extrato PicPay")
        
        return df
    except ImportError:
        print("  ⚠️ Biblioteca pdfplumber não instalada. Instale com: pip install pdfplumber")
        return None
    except Exception as e:
        print(f"Erro ao processar extrato PicPay: {e}")
        return None

def processar_faturas():
    """Processa todos os arquivos de fatura"""
    dados_consolidados = []
    
    # Processar faturas de cartão
    for arquivo in arquivos_fatura:
        if Path(arquivo).exists():
            try:
                df = pd.read_csv(arquivo)
                df['arquivo_origem'] = Path(arquivo).name
                df['fonte'] = 'cartao'
                dados_consolidados.append(df)
                print(f"✓ Processado: {Path(arquivo).name}")
            except Exception as e:
                print(f"✗ Erro ao processar {Path(arquivo).name}: {e}")
    
    # Processar extrato do Itaú (TXT)
    if Path(extrato_itau).exists():
        try:
            df_itau = processar_extrato_itau(extrato_itau)
            if df_itau is not None:
                dados_consolidados.append(df_itau)
                print(f"✓ Processado: {Path(extrato_itau).name}")
        except Exception as e:
            print(f"✗ Erro ao processar extrato Itaú: {e}")
    
    # Processar extrato do PicPay (PDF)
    if Path(extrato_picpay).exists():
        try:
            df_picpay = processar_extrato_picpay(extrato_picpay)
            if df_picpay is not None:
                dados_consolidados.append(df_picpay)
                print(f"✓ Processado: {Path(extrato_picpay).name}")
        except Exception as e:
            print(f"✗ Erro ao processar extrato PicPay: {e}")
    
    if not dados_consolidados:
        print("❌ Nenhum arquivo foi processado com sucesso!")
        return None
    
    # Consolidar todos os dataframes
    df_completo = pd.concat(dados_consolidados, ignore_index=True)
    
    # Converter data
    df_completo['data'] = pd.to_datetime(df_completo['data'], format='%Y-%m-%d', errors='coerce')
    
    # Converter valor para float (se ainda não estiver)
    df_completo['valor'] = pd.to_numeric(df_completo['valor'], errors='coerce')
    
    # Classificar tipo baseado na fonte
    # Cartão: valores positivos = saídas (despesas), negativos = entradas (estornos/pagamentos)
    # Extrato: valores positivos = entradas (receitas), negativos = saídas (despesas)
    def classificar_tipo(row):
        if row['fonte'] == 'cartao':
            return 'Entrada' if row['valor'] < 0 else 'Saída'
        else:  # extrato
            return 'Entrada' if row['valor'] > 0 else 'Saída'
    
    df_completo['tipo'] = df_completo.apply(classificar_tipo, axis=1)
    
    # Converter valores para positivos (valor absoluto)
    df_completo['valor_abs'] = df_completo['valor'].abs()
    
    # Classificar categoria
    df_completo['categoria'] = df_completo['lançamento'].apply(classificar_categoria)
    
    # Adicionar mês/ano
    df_completo['mes_ano'] = df_completo['data'].dt.to_period('M')
    df_completo['mes_nome'] = df_completo['data'].dt.strftime('%B/%Y')
    
    return df_completo

def gerar_relatorio_mensal(df, mes_ano=None):
    """Gera relatório mensal seguindo padrão bancário"""
    
    if mes_ano is None:
        # Usar o mês mais recente
        mes_ano = df['mes_ano'].max()
    
    df_mes = df[df['mes_ano'] == mes_ano].copy()
    
    if len(df_mes) == 0:
        print(f"❌ Nenhuma transação encontrada para {mes_ano}")
        return
    
    mes_nome = df_mes['mes_nome'].iloc[0]
    
    # Cálculos principais
    entradas = df_mes[df_mes['tipo'] == 'Entrada']['valor_abs'].sum()
    saidas = df_mes[df_mes['tipo'] == 'Saída']['valor_abs'].sum()
    
    # Usar renda manual apenas se habilitado E não houver entradas
    usa_renda_manual = False
    if USA_RENDA_MANUAL and entradas == 0:
        entradas = RENDA_MENSAL_MANUAL
        usa_renda_manual = True
    
    resultado = entradas - saidas
    
    # Gastos por categoria
    gastos_categoria = df_mes[df_mes['tipo'] == 'Saída'].groupby('categoria')['valor_abs'].sum().sort_values(ascending=False)
    
    # Percentual da renda comprometida
    perc_comprometida = (saidas / entradas * 100) if entradas > 0 else 0
    
    # Padrões de gasto
    gastos_por_dia = df_mes[df_mes['tipo'] == 'Saída'].groupby(df_mes['data'].dt.day)['valor_abs'].sum()
    dia_maior_gasto = gastos_por_dia.idxmax() if len(gastos_por_dia) > 0 else 'N/A'
    
    # Assinaturas detectadas
    assinaturas = df_mes[(df_mes['categoria'] == 'Assinaturas') & (df_mes['tipo'] == 'Saída')]
    
    # Gerar relatório
    relatorio = f"""
{'='*80}
📊 RELATÓRIO FINANCEIRO MENSAL
{'='*80}

📅 Período: {mes_nome}
📆 Data de Geração: {datetime.now().strftime('%d/%m/%Y às %H:%M')}

{'='*80}
1️⃣ RESUMO EXECUTIVO
{'='*80}

No mês de {mes_nome}, você teve:

💰 Entradas: R$ {entradas:,.2f}
💸 Saídas: R$ {saidas:,.2f}
📊 Resultado: R$ {resultado:,.2f} {'✅' if resultado >= 0 else '⚠️'}

"""
    
    if resultado >= 0:
        relatorio += f"✅ Você gastou MENOS do que ganhou! Sobrou R$ {resultado:,.2f}.\n"
    else:
        relatorio += f"⚠️ ATENÇÃO! Você gastou MAIS do que ganhou em R$ {abs(resultado):,.2f}.\n"
    
    relatorio += f"""
{'='*80}
2️⃣ VISÃO GERAL FINANCEIRA
{'='*80}

┌─────────────────────────────────┬──────────────────┐
│ Indicador                       │ Valor            │
├─────────────────────────────────┼──────────────────┤
│ Total de Entradas               │ R$ {entradas:>12,.2f} │
│ Total de Saídas                 │ R$ {saidas:>12,.2f} │
│ Resultado do Mês                │ R$ {resultado:>12,.2f} │
│ % da Renda Comprometida         │    {perc_comprometida:>9.1f}% │
└─────────────────────────────────┴──────────────────┘

{'='*80}
3️⃣ GASTOS POR CATEGORIA
{'='*80}

"""
    
    for categoria, valor in gastos_categoria.items():
        percentual = (valor / saidas * 100) if saidas > 0 else 0
        alerta = " ⚠️" if percentual > 30 else ""
        relatorio += f"  {categoria:<25} R$ {valor:>10,.2f}  ({percentual:>5.1f}%){alerta}\n"
    
    relatorio += f"\n📌 TOP 3 CATEGORIAS:\n"
    for i, (categoria, valor) in enumerate(gastos_categoria.head(3).items(), 1):
        percentual = (valor / saidas * 100) if saidas > 0 else 0
        relatorio += f"  {i}º {categoria}: R$ {valor:,.2f} ({percentual:.1f}%)\n"
    
    relatorio += f"""
{'='*80}
4️⃣ ENTRADAS DE DINHEIRO
{'='*80}

Total de Entradas: R$ {entradas:,.2f}
"""
    
    if usa_renda_manual:
        relatorio += f"\n💡 **RENDA MANUAL INFORMADA**: R$ {RENDA_MENSAL_MANUAL:,.2f}\n"
        relatorio += "   (Nenhuma entrada detectada no extrato - usando valor configurado)\n\n"
    else:
        relatorio += "\n"
    
    entradas_df = df_mes[df_mes['tipo'] == 'Entrada']
    if len(entradas_df) > 0:
        for _, row in entradas_df.iterrows():
            relatorio += f"  📥 {row['data'].strftime('%d/%m')} - {row['lançamento']}: R$ {row['valor_abs']:,.2f}\n"
        
        # Analisar estabilidade
        if len(entradas_df) > 1:
            relatorio += "\n💡 Análise: Você tem múltiplas fontes de renda neste mês.\n"
        else:
            relatorio += "\n💡 Análise: Renda concentrada em uma única fonte.\n"
    else:
        relatorio += "  ⚠️ Nenhuma entrada registrada neste mês.\n"
    
    relatorio += f"""
{'='*80}
5️⃣ PADRÕES E COMPORTAMENTOS
{'='*80}

📍 Dia do mês com maior gasto: {dia_maior_gasto}
📊 Número de transações: {len(df_mes)}
💳 Gasto médio por transação: R$ {df_mes[df_mes['tipo'] == 'Saída']['valor_abs'].mean():,.2f}

🔄 ASSINATURAS DETECTADAS:
"""
    
    if len(assinaturas) > 0:
        total_assinaturas = assinaturas['valor_abs'].sum()
        relatorio += f"\n  Total em assinaturas: R$ {total_assinaturas:,.2f}/mês\n\n"
        for _, row in assinaturas.iterrows():
            relatorio += f"  • {row['lançamento']}: R$ {row['valor_abs']:,.2f}\n"
    else:
        relatorio += "\n  ✓ Nenhuma assinatura recorrente detectada.\n"
    
    relatorio += f"""
{'='*80}
6️⃣ DETALHAMENTO POR CATEGORIA
{'='*80}

"""
    
    # Detalhamento de cada categoria
    categorias_unicas = df_mes['categoria'].unique()
    
    for categoria in sorted(categorias_unicas):
        df_categoria = df_mes[df_mes['categoria'] == categoria]
        
        # Separar entradas e saídas
        entradas_cat = df_categoria[df_categoria['tipo'] == 'Entrada']
        saidas_cat = df_categoria[df_categoria['tipo'] == 'Saída']
        
        total_entradas_cat = entradas_cat['valor_abs'].sum()
        total_saidas_cat = saidas_cat['valor_abs'].sum()
        saldo_cat = total_entradas_cat - total_saidas_cat
        
        relatorio += f"\n📁 {categoria.upper()}\n"
        relatorio += f"{'─'*80}\n"
        
        if total_entradas_cat > 0:
            relatorio += f"  ✅ Entradas: R$ {total_entradas_cat:,.2f} ({len(entradas_cat)} transações)\n"
            # Listar algumas entradas se não forem muitas
            if len(entradas_cat) <= 5:
                for _, row in entradas_cat.iterrows():
                    relatorio += f"     • {row['data'].strftime('%d/%m')} - {row['lançamento'][:50]}: R$ {row['valor_abs']:,.2f}\n"
            else:
                # Mostrar as 3 maiores
                top_entradas = entradas_cat.nlargest(3, 'valor_abs')
                for _, row in top_entradas.iterrows():
                    relatorio += f"     • {row['data'].strftime('%d/%m')} - {row['lançamento'][:50]}: R$ {row['valor_abs']:,.2f}\n"
                relatorio += f"     ... e mais {len(entradas_cat) - 3} transações\n"
        
        if total_saidas_cat > 0:
            relatorio += f"  ❌ Saídas: R$ {total_saidas_cat:,.2f} ({len(saidas_cat)} transações)\n"
            # Listar algumas saídas se não forem muitas
            if len(saidas_cat) <= 5:
                for _, row in saidas_cat.iterrows():
                    relatorio += f"     • {row['data'].strftime('%d/%m')} - {row['lançamento'][:50]}: R$ {row['valor_abs']:,.2f}\n"
            else:
                # Mostrar as 3 maiores
                top_saidas = saidas_cat.nlargest(3, 'valor_abs')
                for _, row in top_saidas.iterrows():
                    relatorio += f"     • {row['data'].strftime('%d/%m')} - {row['lançamento'][:50]}: R$ {row['valor_abs']:,.2f}\n"
                relatorio += f"     ... e mais {len(saidas_cat) - 3} transações\n"
        
        # Mostrar saldo da categoria
        saldo_icon = "✅" if saldo_cat >= 0 else "⚠️"
        relatorio += f"  📊 Saldo da categoria: R$ {saldo_cat:,.2f} {saldo_icon}\n"
    
    relatorio += f"""
{'='*80}
7️⃣ ALERTAS FINANCEIROS
{'='*80}

"""
    
    alertas = []
    
    # Verificar categorias acima de 30%
    for categoria, valor in gastos_categoria.items():
        percentual = (valor / saidas * 100) if saidas > 0 else 0
        if percentual > 30:
            alertas.append(f"⚠️ Gastos elevados em {categoria} ({percentual:.1f}% do total)")
    
    # Verificar se gastou mais que ganhou
    if resultado < 0:
        alertas.append(f"⚠️ Despesas superiores à renda em R$ {abs(resultado):,.2f}")
    
    # Verificar assinaturas
    if len(assinaturas) > 0:
        perc_assinaturas = (total_assinaturas / entradas * 100) if entradas > 0 else 0
        if perc_assinaturas > 10:
            alertas.append(f"⚠️ Assinaturas comprometendo {perc_assinaturas:.1f}% da renda")
    
    # Verificar dependência de renda
    if len(entradas_df) == 1:
        alertas.append("⚠️ Alta dependência de fonte única de renda")
    
    if alertas:
        for alerta in alertas:
            relatorio += f"  {alerta}\n"
    else:
        relatorio += "  ✅ Nenhum alerta crítico detectado!\n"
    
    relatorio += f"""
{'='*80}
7️⃣ RECOMENDAÇÕES PRÁTICAS
{'='*80}

"""
    
    recomendacoes = []
    
    # Top categoria
    if len(gastos_categoria) > 0:
        top_cat = gastos_categoria.index[0]
        top_valor = gastos_categoria.iloc[0]
        economia_10 = top_valor * 0.1
        recomendacoes.append(f"💡 Reduzir 10% em {top_cat} pode liberar R$ {economia_10:,.2f} por mês")
    
    # Assinaturas
    if len(assinaturas) > 0:
        economia_anual = total_assinaturas * 12
        recomendacoes.append(f"💡 Revisar assinaturas pode gerar economia anual de R$ {economia_anual:,.2f}")
    
    # Alimentação
    if 'Alimentação' in gastos_categoria.index:
        alim_valor = gastos_categoria['Alimentação']
        perc_alim = (alim_valor / saidas * 100) if saidas > 0 else 0
        if perc_alim > 25:
            recomendacoes.append(f"💡 Definir um teto mensal para Alimentação em R$ {alim_valor * 0.8:,.2f}")
    
    # Meta de economia
    if resultado >= 0:
        recomendacoes.append(f"💡 Tente aumentar a economia para 20% da renda = R$ {entradas * 0.2:,.2f}")
    else:
        recomendacoes.append(f"💡 Prioridade: equilibrar receitas e despesas")
    
    for rec in recomendacoes:
        relatorio += f"  {rec}\n"
    
    relatorio += f"""
{'='*80}
9️⃣ CONCLUSÃO SIMPLIFICADA
{'='*80}

"""
    
    # Situação financeira
    if resultado >= 0 and perc_comprometida < 80:
        situacao = "✅ Situação financeira SAUDÁVEL"
    elif resultado >= 0:
        situacao = "⚠️ Situação financeira ESTÁVEL, mas renda muito comprometida"
    else:
        situacao = "🚨 Situação financeira REQUER ATENÇÃO"
    
    relatorio += f"{situacao}\n\n"
    
    # Principais riscos
    if len(alertas) > 0:
        relatorio += f"⚠️ Principais riscos: {len(alertas)} alertas detectados\n"
    else:
        relatorio += "✅ Sem riscos críticos identificados\n"
    
    # Próximo passo
    if resultado < 0:
        relatorio += "🎯 Próximo passo: Reduzir gastos na categoria de maior impacto\n"
    elif perc_comprometida > 90:
        relatorio += "🎯 Próximo passo: Criar reserva de emergência\n"
    else:
        relatorio += "🎯 Próximo passo: Manter disciplina e aumentar taxa de poupança\n"
    
    relatorio += f"\n{'='*80}\n"
    relatorio += "📈 Relatório gerado por: Contador Financeiro Pessoal (Padrão Banco/Fintech)\n"
    relatorio += f"{'='*80}\n"
    
    return relatorio

def gerar_relatorio_consolidado(df):
    """Gera relatório consolidado de todos os meses"""
    
    relatorio = f"""
{'='*80}
📊 RELATÓRIO CONSOLIDADO - VISÃO GERAL
{'='*80}

📅 Período analisado: {df['data'].min().strftime('%m/%Y')} a {df['data'].max().strftime('%m/%Y')}
📆 Data de geração: {datetime.now().strftime('%d/%m/%Y às %H:%M')}

{'='*80}
📈 EVOLUÇÃO MENSAL
{'='*80}

"""
    
    # Agrupar por mês
    resumo_mensal = df.groupby(['mes_ano', 'tipo'])['valor_abs'].sum().unstack(fill_value=0)
    
    if 'Entrada' not in resumo_mensal.columns:
        resumo_mensal['Entrada'] = 0
    if 'Saída' not in resumo_mensal.columns:
        resumo_mensal['Saída'] = 0
    
    resumo_mensal['Resultado'] = resumo_mensal['Entrada'] - resumo_mensal['Saída']
    
    relatorio += "┌──────────────┬─────────────┬─────────────┬─────────────┐\n"
    relatorio += "│ Mês          │ Entradas    │ Saídas      │ Resultado   │\n"
    relatorio += "├──────────────┼─────────────┼─────────────┼─────────────┤\n"
    
    for mes, row in resumo_mensal.iterrows():
        resultado_icon = "✅" if row['Resultado'] >= 0 else "⚠️"
        relatorio += f"│ {str(mes):<12} │ R$ {row['Entrada']:>8,.2f} │ R$ {row['Saída']:>8,.2f} │ R$ {row['Resultado']:>8,.2f} {resultado_icon} │\n"
    
    relatorio += "└──────────────┴─────────────┴─────────────┴─────────────┘\n"
    
    # Médias
    media_entradas = resumo_mensal['Entrada'].mean()
    media_saidas = resumo_mensal['Saída'].mean()
    media_resultado = resumo_mensal['Resultado'].mean()
    
    relatorio += f"""
{'='*80}
📊 MÉDIAS DO PERÍODO
{'='*80}

  Média de Entradas:  R$ {media_entradas:,.2f}
  Média de Saídas:    R$ {media_saidas:,.2f}
  Média de Resultado: R$ {media_resultado:,.2f}

{'='*80}
🏆 GASTOS POR CATEGORIA (TODO O PERÍODO)
{'='*80}

"""
    
    gastos_totais = df[df['tipo'] == 'Saída'].groupby('categoria')['valor_abs'].sum().sort_values(ascending=False)
    total_geral = gastos_totais.sum()
    
    for categoria, valor in gastos_totais.items():
        percentual = (valor / total_geral * 100) if total_geral > 0 else 0
        relatorio += f"  {categoria:<25} R$ {valor:>12,.2f}  ({percentual:>5.1f}%)\n"
    
    relatorio += f"\n{'='*80}\n"
    
    return relatorio

# Executar análise
if __name__ == "__main__":
    print("🏦 Contador Financeiro Pessoal - Iniciando análise...\n")
    
    # Processar faturas
    df = processar_faturas()
    
    if df is not None:
        print(f"\n✅ {len(df)} transações processadas com sucesso!\n")
        
        # Gerar relatório consolidado
        print("📊 Gerando relatório consolidado...")
        relatorio_consolidado = gerar_relatorio_consolidado(df)
        
        # Salvar relatório consolidado
        with open('/Users/joaogabriel/Notas/relatorio_consolidado.txt', 'w', encoding='utf-8') as f:
            f.write(relatorio_consolidado)
        print("✅ Relatório consolidado salvo em: relatorio_consolidado.txt\n")
        
        # Identificar meses com múltiplas fontes de dados
        print("\n📊 Analisando cobertura de dados por mês...")
        
        meses_com_fontes = df.groupby('mes_ano')['fonte'].apply(lambda x: set(x)).to_dict()
        
        # Ordenar meses por número de fontes (priorizar meses com mais dados)
        meses_ordenados = sorted(
            meses_com_fontes.items(),
            key=lambda x: (len(x[1]), x[0]),  # Ordena por número de fontes, depois por data
            reverse=True
        )
        
        print("\n🔍 Cobertura de dados por mês:")
        for mes, fontes in meses_ordenados[:10]:
            total_transacoes = len(df[df['mes_ano'] == mes])
            fontes_str = ', '.join(sorted(fontes))
            print(f"  {mes}: {len(fontes)} fonte(s) - {total_transacoes} transações ({fontes_str})")
        
        # Gerar relatórios dos meses com mais dados (priorizar meses com múltiplas fontes)
        print("\n📝 Gerando relatórios dos meses com mais dados...")
        
        meses_para_relatorio = []
        
        # Primeiro: meses com 3 fontes (cartão + extrato_itau + extrato_picpay)
        for mes, fontes in meses_ordenados:
            if len(fontes) >= 3:
                meses_para_relatorio.append(mes)
        
        # Depois: meses com 2 fontes (se não tiver suficientes com 3)
        if len(meses_para_relatorio) < 5:
            for mes, fontes in meses_ordenados:
                if len(fontes) == 2 and mes not in meses_para_relatorio:
                    meses_para_relatorio.append(mes)
                    if len(meses_para_relatorio) >= 5:
                        break
        
        # Por último: adicionar os mais recentes até ter 5 relatórios
        if len(meses_para_relatorio) < 5:
            meses_recentes = sorted(df['mes_ano'].unique(), reverse=True)
            for mes in meses_recentes:
                if mes not in meses_para_relatorio:
                    meses_para_relatorio.append(mes)
                    if len(meses_para_relatorio) >= 5:
                        break
        
        # Ordenar cronologicamente para gerar
        meses_para_relatorio = sorted(meses_para_relatorio, reverse=True)
        
        for mes in meses_para_relatorio[:5]:
            fontes_mes = meses_com_fontes[mes]
            total_trans = len(df[df['mes_ano'] == mes])
            print(f"📅 Gerando relatório para {mes} ({len(fontes_mes)} fontes, {total_trans} transações)...")
            relatorio = gerar_relatorio_mensal(df, mes)
            
            # Salvar relatório mensal
            nome_arquivo = f'/Users/joaogabriel/Notas/relatorio_{str(mes).replace("/", "_")}.txt'
            with open(nome_arquivo, 'w', encoding='utf-8') as f:
                f.write(relatorio)
            print(f"✅ Salvo em: {Path(nome_arquivo).name}\n")
        
        print("="*80)
        print("✅ ANÁLISE COMPLETA!")
        print("="*80)
        print("\n📂 Arquivos gerados em: /Users/joaogabriel/Notas/")
        print("  • relatorio_consolidado.txt")
        print("  • relatorio_[mes].txt (meses com mais dados)")
        print("\n💡 Dica: Abra os arquivos .txt para visualizar os relatórios detalhados")
