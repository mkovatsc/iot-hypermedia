#!/bin/bash
#*******************************************************************************
# Copyright (c) 2016 Institute for Pervasive Computing, ETH Zurich.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# and Eclipse Distribution License v1.0 which accompany this distribution.
#
# The Eclipse Public License is available at
#    http://www.eclipse.org/legal/epl-v10.html
# and the Eclipse Distribution License is available at
#    http://www.eclipse.org/org/documents/edl-v10.html.
#
# Contributors:
#    Matthias Kovatsch - creator and main architect
#    Yassin N. Hassan - architect and implementation
#*******************************************************************************
cat ${1}.mkd |  sed  's/````javascript/\\begin{lstlisting}[language=java]/' |  sed  's/```java/\\begin{lstlisting}[language=java]/'|  sed  's/````/\\end{lstlisting}/' |  sed  's/```/\\end{lstlisting}/' |  sed  's/ `/ \\texttt{/g' |  sed  's/`/}/g' > ${1}.tex